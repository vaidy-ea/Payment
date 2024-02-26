package com.mgm.pd.cp.resortpayment.util.authorize;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mgm.pd.cp.payment.common.constant.AuthType;
import com.mgm.pd.cp.payment.common.dto.opera.Card;
import com.mgm.pd.cp.payment.common.dto.opera.TransactionAmount;
import com.mgm.pd.cp.resortpayment.constants.BooleanValue;
import com.mgm.pd.cp.resortpayment.dto.*;
import com.mgm.pd.cp.resortpayment.dto.authorize.CPPaymentAuthorizationRequest;
import com.mgm.pd.cp.resortpayment.dto.incrementalauth.IncrementalRouterRequestJson;
import com.mgm.pd.cp.resortpayment.dto.router.RouterRequest;
import com.mgm.pd.cp.resortpayment.util.common.PaymentProcessingServiceHelper;
import lombok.AllArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import static com.mgm.pd.cp.payment.common.constant.ApplicationConstants.*;

@Component
@AllArgsConstructor
public class AuthorizeToRouterConverter implements Converter<CPPaymentAuthorizationRequest, RouterRequest> {
    public static final String NULL = "null";
    ObjectMapper mapper;
    private PaymentProcessingServiceHelper helper;

    @Override
    public RouterRequest convert(CPPaymentAuthorizationRequest source) {
        TransactionDetails transactionDetails = source.getTransactionDetails();
        TransactionAmount transactionAmount = transactionDetails.getTransactionAmount();
        Customer customer = transactionDetails.getCustomer();
        Address billingAddress = customer.getBillingAddress();
        CurrencyConversion currencyConversion = transactionDetails.getCurrencyConversion();
        Card card = transactionDetails.getCard();
        Merchant merchant = transactionDetails.getMerchant();
        String roomRate = helper.getValueFromSaleDetails(source, ROOM_RATE);
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
                .propertyCode(helper.getValueFromSaleDetails(source, PROPERTY_IDENTIFIER))
                .chainCode(helper.getValueFromSaleDetails(source, PROPERTY_CHAIN_IDENTIFIER))
                .checkOutDate(helper.getValueFromSaleDetails(source, CHECK_IN_DATE))
                .checkInDate(helper.getValueFromSaleDetails(source, CHECK_OUT_DATE))
                .originDate(helper.getValueFromSaleDetails(source, ORIGIN_DATE))
                .roomNum(helper.getValueFromSaleDetails(source, ROOM_NUMBER))
                .roomRate(!roomRate.equals(NULL) ? Double.valueOf(roomRate) : null)
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
