package com.mgm.pd.cp.resortpayment.util.refund;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mgm.pd.cp.payment.common.constant.BooleanValue;
import com.mgm.pd.cp.payment.common.dto.opera.Card;
import com.mgm.pd.cp.payment.common.dto.opera.TransactionAmount;
import com.mgm.pd.cp.resortpayment.dto.*;
import com.mgm.pd.cp.resortpayment.dto.refund.CPPaymentRefundRequest;
import com.mgm.pd.cp.resortpayment.dto.refund.RefundRouterRequestJson;
import com.mgm.pd.cp.resortpayment.dto.router.RouterRequest;
import com.mgm.pd.cp.resortpayment.util.common.PaymentProcessingServiceHelper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

import static com.mgm.pd.cp.payment.common.constant.ApplicationConstants.*;

@Component
@AllArgsConstructor
public class RefundToRouterConverter {
    ObjectMapper mapper;
    private PaymentProcessingServiceHelper helper;

    public RouterRequest convert(CPPaymentRefundRequest request) {
        BaseTransactionDetails baseTransactionDetails = helper.getBaseTransactionDetails(request);
        TransactionDetails transactionDetails = request.getTransactionDetails();
        TransactionAmount transactionAmount = transactionDetails.getTransactionAmount();
        CurrencyConversion currencyConversion = transactionDetails.getCurrencyConversion();
        Card card = transactionDetails.getCard();
        Merchant merchant = transactionDetails.getMerchant();
        Customer customer = transactionDetails.getCustomer();
        String roomRate = helper.getValueFromSaleDetails(baseTransactionDetails, ROOM_RATE);
        String clerkIdentifier = merchant.getClerkIdentifier();
        String originalTransactionIdentifier = request.getOriginalTransactionIdentifier();
        Optional<RefundRouterRequestJson> requestJson= Optional.ofNullable(RefundRouterRequestJson.builder()
                .amount(transactionAmount.getDetailedAmount().getAmount())
                .totalAuthAmount(transactionAmount.getCumulativeAmount())
                .binCurrencyCode(currencyConversion.getBinCurrencyCode())
                .binRate(currencyConversion.getBinCurrencyCode())
                .cardType(card.getCardType())
                .cardPresent(BooleanValue.getEnumByString(transactionDetails.getIsCardPresent().toString()))
                .currencyIndicator(transactionAmount.getCurrencyIndicator())
                .arrivalDate(helper.getValueFromSaleDetails(baseTransactionDetails, CHECK_IN_DATE))
                .cardExpirationDate(card.getExpiryDate())
                .dccAmount(Double.valueOf(currencyConversion.getAmount()))
                .dccControlNumber(Double.valueOf(currencyConversion.getConversionIdentifier()))
                .chainCode(helper.getValueFromSaleDetails(baseTransactionDetails, PROPERTY_CHAIN_IDENTIFIER))
                .guestName(customer.getFullName())
                //.installments(transactionDetails.getSaleItem().getSaleDetails().getEstimatedDuration())
                .merchantID(merchant.getMerchantIdentifier())
                .propertyCode(helper.getValueFromSaleDetails(baseTransactionDetails, PROPERTY_IDENTIFIER))
                .version(merchant.getVersion())
                .dCCFlag(currencyConversion.getConversionFlag())
                .corelationId(card.getCardIssuerName())
                .roomNum(helper.getValueFromSaleDetails(baseTransactionDetails, ROOM_NUMBER))
                .roomRate(!roomRate.equals("null") ? Double.valueOf(roomRate) : null)
                .startDate(card.getStartDate())
                .cardNumber(card.getMaskedCardNumber())
                .trackIndicator(card.getTrack1())
                .usageType(card.getTokenValue())
                .workstation(merchant.getTerminalIdentifier())
                .departureDate(helper.getValueFromSaleDetails(baseTransactionDetails, CHECK_OUT_DATE))
                .resvNameID(transactionDetails.getSaleItem().getSaleReferenceIdentifier())
                .vendorTranID(request.getGatewayInfo().getGatewayTransactionIdentifier())
                .sequenceNumber(request.getTransactionIdentifier())
                .originalAuthSequence(Objects.nonNull(originalTransactionIdentifier) ? Long.valueOf(originalTransactionIdentifier) : null)
                .transDate(request.getTransactionDateTime())
                .messageType(String.valueOf(request.getTransactionType()))
                .clientID(card.getCardIssuerName())
                .corelationId(card.getCardIssuerIdentification())
                .approvalCode(card.getTokenType())
                .clerkId(Objects.nonNull(clerkIdentifier) ? Long.valueOf(clerkIdentifier) : null)
                .dateTime(String.valueOf(LocalDateTime.now()))
                .billingZIP(customer.getBillingAddress().getPostCode())
                .build());
        String requestJsonAsString;
        try {
            requestJsonAsString = mapper.writeValueAsString(requestJson);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return RouterRequest.builder()
                .operation(REFUND_OPERATION)
                .gatewayId(SHIFT4_GATEWAY_ID)
                .requestJson(requestJsonAsString).build();
    }
}