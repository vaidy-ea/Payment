package com.mgm.pd.cp.resortpayment.util.incremental;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mgm.pd.cp.payment.common.constant.AuthType;
import com.mgm.pd.cp.payment.common.dto.opera.Card;
import com.mgm.pd.cp.payment.common.dto.opera.TransactionAmount;
import com.mgm.pd.cp.resortpayment.constants.BooleanValue;
import com.mgm.pd.cp.resortpayment.dto.*;
import com.mgm.pd.cp.resortpayment.dto.incrementalauth.CPPaymentIncrementalAuthRequest;
import com.mgm.pd.cp.resortpayment.dto.incrementalauth.IncrementalRouterRequestJson;
import com.mgm.pd.cp.resortpayment.dto.router.RouterRequest;
import com.mgm.pd.cp.resortpayment.util.common.PaymentProcessingServiceHelper;
import lombok.AllArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.Objects;

import static com.mgm.pd.cp.payment.common.constant.ApplicationConstants.AUTHORIZE_OPERATION;
import static com.mgm.pd.cp.payment.common.constant.ApplicationConstants.SHIFT4_GATEWAY_ID;

@Component
@AllArgsConstructor
public class IncrementalToRouterConverter implements Converter<CPPaymentIncrementalAuthRequest, RouterRequest> {
    ObjectMapper mapper;
    private PaymentProcessingServiceHelper helper;

    @Override
    public RouterRequest convert(CPPaymentIncrementalAuthRequest source) {
        TransactionDetails transactionDetails = source.getTransactionDetails();
        TransactionAmount transactionAmount = transactionDetails.getTransactionAmount();
        Customer customer = transactionDetails.getCustomer();
        Address billingAddress = customer.getBillingAddress();
        CurrencyConversion currencyConversion = transactionDetails.getCurrencyConversion();
        Card card = transactionDetails.getCard();
        Merchant merchant = transactionDetails.getMerchant();
        IncrementalRouterRequestJson requestJson = IncrementalRouterRequestJson.builder()
                .authorizationAmount(transactionAmount.getRequestedAmount())
                .totalAuthAmount(transactionAmount.getCumulativeAmount())
                .currencyIndicator(transactionAmount.getCurrencyIndicator())
                .guestName(customer.getFullName())
                .billingAddress1(billingAddress.getStreetName())
                .billingAddress2(billingAddress.getAddressLine())
                .billingCity(billingAddress.getTownName())
                .billingState(billingAddress.getCountrySubDivision())
                .billingZIP(billingAddress.getPostCode())
                .dccAmount(Double.valueOf(currencyConversion.getAmount()))
                .dCCFlag(currencyConversion.getConversionIdentifier())
                .binRate(currencyConversion.getBinCurrencyRate())
                .uniqueID(card.getTokenValue())
                .binCurrencyCode(currencyConversion.getBinCurrencyCode())
                .cardNumber(card.getMaskedCardNumber())
                .cardExpirationDate(card.getExpiryDate())
                .cardPresent(BooleanValue.getEnumByString(transactionDetails.getIsCardPresent().toString()))
                .cardType(String.valueOf(card.getCardType()))
                .cID(card.getCardIssuerIdentification())
                .trackData(card.getTrack1())
                //.trackLength(source.getTrackLength())
                //.trackIndicator(source.getTrackIndicator())
                .startDate(card.getStartDate())
                .issueNumber(Integer.valueOf(card.getSequenceNumber()))
                //.usageType(source.getUsageType())
                .merchantID(merchant.getMerchantIdentifier())
                .version(merchant.getVersion())
                .workstation(merchant.getTerminalIdentifier())
                //TODO : Uncomment
                .propertyCode(helper.getValueByName(source, "propertyIdentifier"))
                .chainCode(helper.getValueByName(source, "propertyChainIdentifier"))
                .checkOutDate(helper.getValueByName(source, "checkOutDate"))
                .checkInDate(helper.getValueByName(source, "checkInDate"))
                .originDate(helper.getValueByName(source, "originDate"))
                .roomNum(helper.getValueByName(source, "roomNumber"))
                .roomRate(Objects.nonNull(helper.getValueByName(source, "roomRate")) ? Double.valueOf(helper.getValueByName(source, "roomRate")) : null)
                .resvNameID(transactionDetails.getSaleItem().getSaleReferenceIdentifier())
                .vendorTranID(source.getGatewayInfo().getGatewayTransactionIdentifier())
                .balance(transactionDetails.getTransactionAmount().getBalanceAmount())
                .sequenceNumber(source.getTransactionIdentifier())
                .originalAuthSequence(Long.valueOf(source.getOriginalTransactionIdentifier()))
                .transDate(source.getTransactionDateTime())
                .authType(AuthType.valueOf(source.getTransactionType()))
                //.aVSStatus(source.getAVSStatus())
                .clientID(source.getClientID())
                .corelationId(source.getCorelationId())
                .incrementalAuthInvoiceId(source.getIncrementalAuthInvoiceId())
                .dateTime(source.getDateTime())
                .clerkId(source.getClerkId())
                .build();

        String requestJsonAsString;
        try {
            requestJsonAsString = mapper.writeValueAsString(requestJson);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return RouterRequest.builder()
                .operation(AUTHORIZE_OPERATION)
                .gatewayId(SHIFT4_GATEWAY_ID)
                .requestJson(requestJsonAsString).build();
    }
}