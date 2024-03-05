package com.mgm.pd.cp.resortpayment.util.incremental;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mgm.pd.cp.payment.common.constant.BooleanValue;
import com.mgm.pd.cp.payment.common.dto.opera.Card;
import com.mgm.pd.cp.payment.common.dto.opera.TransactionAmount;
import com.mgm.pd.cp.resortpayment.dto.*;
import com.mgm.pd.cp.resortpayment.dto.incrementalauth.CPPaymentIncrementalAuthRequest;
import com.mgm.pd.cp.resortpayment.dto.incrementalauth.IncrementalRouterRequestJson;
import com.mgm.pd.cp.resortpayment.dto.router.RouterRequest;
import com.mgm.pd.cp.resortpayment.util.common.PaymentProcessingServiceHelper;
import lombok.AllArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Objects;

import static com.mgm.pd.cp.payment.common.constant.ApplicationConstants.*;

@Component
@AllArgsConstructor
public class IncrementalToRouterConverter implements Converter<CPPaymentIncrementalAuthRequest, RouterRequest> {
    private ObjectMapper mapper;
    private PaymentProcessingServiceHelper helper;

    @Override
    public RouterRequest convert(CPPaymentIncrementalAuthRequest request) {
        BaseTransactionDetails baseTransactionDetails = helper.getBaseTransactionDetails(request);
        TransactionDetails transactionDetails = request.getTransactionDetails();
        TransactionAmount transactionAmount = transactionDetails.getTransactionAmount();
        Customer customer = transactionDetails.getCustomer();
        Address billingAddress = customer.getBillingAddress();
        CurrencyConversion currencyConversion = transactionDetails.getCurrencyConversion();
        Card card = transactionDetails.getCard();
        Merchant merchant = transactionDetails.getMerchant();
        String roomRate = helper.getValueFromSaleDetails(baseTransactionDetails, ROOM_RATE);
        String clerkIdentifier = merchant.getClerkIdentifier();
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
                .cardType(card.getCardType())
                .cID(card.getCardIssuerIdentification())
                .trackData(card.getTrack1())
                //.trackLength(request.getTrackLength())
                //.trackIndicator(request.getTrackIndicator())
                .startDate(card.getStartDate())
                .issueNumber(Integer.valueOf(card.getSequenceNumber()))
                //.usageType(request.getUsageType())
                .merchantID(merchant.getMerchantIdentifier())
                .version(merchant.getVersion())
                .workstation(merchant.getTerminalIdentifier())
                .propertyCode(helper.getValueFromSaleDetails(baseTransactionDetails, PROPERTY_IDENTIFIER))
                .chainCode(helper.getValueFromSaleDetails(baseTransactionDetails, PROPERTY_CHAIN_IDENTIFIER))
                .checkOutDate(helper.getValueFromSaleDetails(baseTransactionDetails, CHECK_OUT_DATE))
                .checkInDate(helper.getValueFromSaleDetails(baseTransactionDetails, CHECK_IN_DATE))
                .originDate(helper.getValueFromSaleDetails(baseTransactionDetails, ORIGIN_DATE))
                .roomNum(helper.getValueFromSaleDetails(baseTransactionDetails, ROOM_NUMBER))
                .roomRate(!roomRate.equals(NULL) ? Double.valueOf(roomRate) : null)
                .resvNameID(transactionDetails.getSaleItem().getSaleReferenceIdentifier())
                .vendorTranID(request.getGatewayInfo().getGatewayTransactionIdentifier())
                .balance(transactionDetails.getTransactionAmount().getBalanceAmount())
                .sequenceNumber(request.getTransactionIdentifier())
                .originalAuthSequence(Long.valueOf(request.getOriginalTransactionIdentifier()))
                .transDate(request.getTransactionDateTime())
                .authType(request.getTransactionType())
                //.aVSStatus(request.getAVSStatus())
                .clientID(request.getClientID())
                .corelationId(request.getCorelationId())
                .incrementalAuthInvoiceId(request.getAuthChainId())
                .dateTime(String.valueOf(LocalDateTime.now()))
                .clerkId(Objects.nonNull(clerkIdentifier) ? Long.valueOf(clerkIdentifier) : null)
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
