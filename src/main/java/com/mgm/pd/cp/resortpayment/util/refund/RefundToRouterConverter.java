package com.mgm.pd.cp.resortpayment.util.refund;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mgm.pd.cp.payment.common.constant.BooleanValue;
import com.mgm.pd.cp.payment.common.constant.OrderType;
import com.mgm.pd.cp.payment.common.dto.CPRequestHeaders;
import com.mgm.pd.cp.payment.common.dto.opera.Card;
import com.mgm.pd.cp.payment.common.dto.opera.TransactionAmount;
import com.mgm.pd.cp.resortpayment.dto.common.BaseTransactionDetails;
import com.mgm.pd.cp.resortpayment.dto.common.Customer;
import com.mgm.pd.cp.resortpayment.dto.common.Merchant;
import com.mgm.pd.cp.resortpayment.dto.common.TransactionDetails;
import com.mgm.pd.cp.resortpayment.dto.refund.CPPaymentRefundRequest;
import com.mgm.pd.cp.resortpayment.dto.refund.RefundRouterRequestJson;
import com.mgm.pd.cp.resortpayment.dto.router.RouterRequest;
import com.mgm.pd.cp.resortpayment.util.common.PaymentProcessingServiceHelper;
import lombok.AllArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;

import static com.mgm.pd.cp.payment.common.constant.ApplicationConstants.*;

/**
 * This class is responsible for taking a class and converting it to RouterRequest compatible
 */
@Component
@AllArgsConstructor
public class RefundToRouterConverter implements Converter<CPPaymentRefundRequest, RouterRequest> {
    ObjectMapper mapper;
    private PaymentProcessingServiceHelper helper;

    @Override
    public RouterRequest convert(CPPaymentRefundRequest request) {
        BaseTransactionDetails baseTransactionDetails = helper.getBaseTransactionDetails(request);
        HashMap<String, String> valueFromSaleDetails = helper.getSaleDetailsObject(baseTransactionDetails);
        TransactionDetails transactionDetails = request.getTransactionDetails();
        String saleType = baseTransactionDetails.getSaleItem().getSaleType();
        saleType = Objects.nonNull(saleType) ? saleType: "";
        TransactionAmount transactionAmount = transactionDetails.getTransactionAmount();
        Card card = transactionDetails.getCard();
        Merchant merchant = transactionDetails.getMerchant();
        Customer customer = transactionDetails.getCustomer();
        String clerkIdentifier = merchant.getClerkIdentifier();
        CPRequestHeaders headers = request.getHeaders();
        String originalTransactionIdentifier = request.getOriginalTransactionIdentifier();
        Boolean isCardPresent = transactionDetails.getIsCardPresent();
        Optional<RefundRouterRequestJson> requestJson= Optional.ofNullable(RefundRouterRequestJson.builder()
                .dateTime(String.valueOf(LocalDateTime.now()))
                .totalAuthAmount(transactionAmount.getCumulativeAmount())
                .currencyIndicator(transactionAmount.getCurrencyIndicator())
                .guestName(customer.getFullName())
                .billingZIP(customer.getBillingAddress().getPostCode())
                .cardNumber(card.getTokenValue())
                .cardExpirationDate(card.getExpiryDate())
                .cardPresent(BooleanValue.getEnumByString(isCardPresent.toString()))
                .workstation(merchant.getTerminalIdentifier())
                .resvNameID(transactionDetails.getSaleItem().getSaleReferenceIdentifier())
                .roomNum(saleType.equals(OrderType.Hotel.name()) ? valueFromSaleDetails.get(ROOM_NUMBER) : valueFromSaleDetails.get(TICKET_NUMBER))
                .vendorTranID(request.getAuthChainId())
                .sequenceNumber(request.getTransactionIdentifier())
                .originalAuthSequence(Objects.nonNull(originalTransactionIdentifier) ? Long.valueOf(originalTransactionIdentifier) : null)
                .transDate(request.getTransactionDateTime())
                .clerkId(Objects.nonNull(clerkIdentifier) ? Long.valueOf(clerkIdentifier) : null)
                .clientID(headers.getClientId())
                .corelationId(headers.getCorrelationId())
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