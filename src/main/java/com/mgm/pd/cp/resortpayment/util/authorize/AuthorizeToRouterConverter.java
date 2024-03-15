package com.mgm.pd.cp.resortpayment.util.authorize;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mgm.pd.cp.payment.common.constant.BooleanValue;
import com.mgm.pd.cp.payment.common.constant.OrderType;
import com.mgm.pd.cp.payment.common.dto.CPRequestHeaders;
import com.mgm.pd.cp.payment.common.dto.opera.Card;
import com.mgm.pd.cp.payment.common.dto.opera.TransactionAmount;
import com.mgm.pd.cp.resortpayment.dto.authorize.AuthorizationRouterRequestJson;
import com.mgm.pd.cp.resortpayment.dto.authorize.CPPaymentAuthorizationRequest;
import com.mgm.pd.cp.resortpayment.dto.common.*;
import com.mgm.pd.cp.resortpayment.dto.router.RouterRequest;
import com.mgm.pd.cp.resortpayment.util.common.PaymentProcessingServiceHelper;
import lombok.AllArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Objects;

import static com.mgm.pd.cp.payment.common.constant.ApplicationConstants.*;

/**
 * This class is responsible for taking a class and converting it to RouterRequest compatible
 */
@Component
@AllArgsConstructor
public class AuthorizeToRouterConverter implements Converter<CPPaymentAuthorizationRequest, RouterRequest> {
    private ObjectMapper mapper;
    private PaymentProcessingServiceHelper helper;

    @Override
    public RouterRequest convert(CPPaymentAuthorizationRequest request) {
        BaseTransactionDetails baseTransactionDetails = helper.getBaseTransactionDetails(request);
        String saleType = baseTransactionDetails.getSaleItem().getSaleType();
        HashMap<String, String> valueFromSaleDetails = helper.getSaleDetailsObject(baseTransactionDetails);
        TransactionDetails transactionDetails = request.getTransactionDetails();
        TransactionAmount transactionAmount = transactionDetails.getTransactionAmount();
        Customer customer = transactionDetails.getCustomer();
        Address billingAddress = customer.getBillingAddress();
        /*CurrencyConversion currencyConversion = transactionDetails.getCurrencyConversion();*/
        Card card = transactionDetails.getCard();
        Merchant merchant = transactionDetails.getMerchant();
        /*String roomRate = valueFromSaleDetails.get(ROOM_RATE);*/
        CPRequestHeaders headers = request.getHeaders();
        String clerkIdentifier = merchant.getClerkIdentifier();
        AuthorizationRouterRequestJson requestJson = AuthorizationRouterRequestJson.builder()
                .dateTime(String.valueOf(LocalDateTime.now()))
                .totalAuthAmount(transactionAmount.getCumulativeAmount())
                .currencyIndicator(transactionAmount.getCurrencyIndicator())
                .guestName(customer.getFullName())
                .billingAddress1(billingAddress.getStreetName())
                .billingAddress2(billingAddress.getAddressLine())
                .billingZIP(billingAddress.getPostCode())
                .cardNumber(card.getMaskedCardNumber())
                .cardExpirationDate(card.getExpiryDate())
                .cardPresent(BooleanValue.getEnumByString(transactionDetails.getIsCardPresent().toString()))
                .workstation(merchant.getTerminalIdentifier())
                .checkOutDate(valueFromSaleDetails.get(CHECK_OUT_DATE))
                .checkInDate(valueFromSaleDetails.get(CHECK_IN_DATE))
                .roomNum(saleType.equals(OrderType.Hotel.name()) ? valueFromSaleDetails.get(ROOM_NUMBER) : valueFromSaleDetails.get(TICKET_NUMBER))
                .resvNameID(transactionDetails.getSaleItem().getSaleReferenceIdentifier())
                .sequenceNumber(request.getTransactionIdentifier())
                .originalAuthSequence(Long.valueOf(request.getOriginalTransactionIdentifier()))
                .transDate(request.getTransactionDateTime())
                .authType(request.getTransactionType())
                .clerkId(Objects.nonNull(clerkIdentifier) ? Long.valueOf(clerkIdentifier) : null)
                .clientID(headers.getClientId())
                .corelationId(headers.getCorrelationId())
                .build();

                /*.authorizationAmount(transactionAmount.getRequestedAmount())
                .billingCity(billingAddress.getTownName())
                .billingState(billingAddress.getCountrySubDivision())
                .dccAmount(Double.valueOf(currencyConversion.getAmount()))
                .dCCFlag(currencyConversion.getConversionIdentifier())
                .binCurrencyCode(currencyConversion.getBinCurrencyCode())
                .binRate(currencyConversion.getBinCurrencyRate())
                .uniqueID(card.getTokenValue())
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
                .propertyCode(valueFromSaleDetails.get(PROPERTY_IDENTIFIER))
                .chainCode(valueFromSaleDetails.get(PROPERTY_CHAIN_IDENTIFIER))
                .originDate(valueFromSaleDetails.get(ORIGIN_DATE))
                .roomRate(!roomRate.equals(NULL) ? Double.valueOf(roomRate) : null)
                .balance(transactionDetails.getTransactionAmount().getBalanceAmount())
                //.aVSStatus(request.getAVSStatus())
                .build();*/
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
