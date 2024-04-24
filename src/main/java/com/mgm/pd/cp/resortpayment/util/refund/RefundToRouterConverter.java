package com.mgm.pd.cp.resortpayment.util.refund;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mgm.pd.cp.payment.common.constant.BooleanValue;
import com.mgm.pd.cp.payment.common.constant.OrderType;
import com.mgm.pd.cp.payment.common.dto.CPRequestHeaders;
import com.mgm.pd.cp.payment.common.dto.opera.Card;
import com.mgm.pd.cp.payment.common.dto.opera.DetailedAmount;
import com.mgm.pd.cp.payment.common.dto.opera.TransactionAmount;
import com.mgm.pd.cp.resortpayment.dto.common.*;
import com.mgm.pd.cp.resortpayment.dto.refund.CPPaymentRefundRequest;
import com.mgm.pd.cp.resortpayment.dto.refund.RefundRouterRequestJson;
import com.mgm.pd.cp.resortpayment.dto.router.RouterRequest;
import com.mgm.pd.cp.resortpayment.util.common.PaymentProcessingServiceHelper;
import lombok.AllArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

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
        HashMap<String, String> valueFromSaleDetails = Objects.nonNull(helper.getSaleDetailsObject(baseTransactionDetails)) ? helper.getSaleDetailsObject(baseTransactionDetails) : new HashMap<>();
        TransactionDetails transactionDetails = request.getTransactionDetails();
        SaleItem saleItem = Objects.nonNull(baseTransactionDetails.getSaleItem()) ? baseTransactionDetails.getSaleItem() : new SaleItem();
        String saleType = saleItem.getSaleType();
        saleType = Objects.nonNull(saleType) ? saleType: "";
        TransactionAmount transactionAmount = transactionDetails.getTransactionAmount();
        Card card = transactionDetails.getCard();
        Customer customer = Objects.nonNull(transactionDetails.getCustomer()) ? transactionDetails.getCustomer() : new Customer();
        Address billingAddress = Objects.nonNull(customer.getBillingAddress()) ? customer.getBillingAddress() : new Address();
        CPRequestHeaders headers = request.getHeaders();
        String originalTransactionIdentifier = request.getOriginalTransactionIdentifier();
        Boolean isCardPresent = Objects.nonNull(transactionDetails.getIsCardPresent()) ? transactionDetails.getIsCardPresent() : Boolean.TRUE;
        DetailedAmount detailedAmount = Objects.nonNull(transactionAmount.getDetailedAmount()) ? transactionAmount.getDetailedAmount() : new DetailedAmount();
        Optional<RefundRouterRequestJson> requestJson= Optional.ofNullable(RefundRouterRequestJson.builder()
                .totalAuthAmount(transactionAmount.getRequestedAmount())
                .currencyIndicator(transactionAmount.getCurrencyIndicator())
                .guestName(customer.getFullName())
                .billingZIP(billingAddress.getPostCode())
                .cardNumber(card.getTokenValue())
                .cardExpirationDate(card.getExpiryDate())
                .cardPresent(BooleanValue.getEnumByString(isCardPresent.toString()))
                .resvNameID(saleItem.getSaleReferenceIdentifier())
                .roomNum(saleType.equals(OrderType.Hotel.name()) ? valueFromSaleDetails.get(ROOM_NUMBER) : valueFromSaleDetails.get(TICKET_NUMBER))
                .vendorTranID(request.getTransactionAuthChainId())
                .sequenceNumber(request.getTransactionIdentifier())
                .originalAuthSequence(Objects.nonNull(originalTransactionIdentifier) ? Long.valueOf(originalTransactionIdentifier) : null)
                .transDate(request.getTransactionDateTime())
                .clientID(headers.getClientId())
                .corelationId(headers.getCorrelationId())
                .taxAmount(detailedAmount.getTax())
                .build());
        String requestJsonAsString;
        try {
            requestJsonAsString = mapper.writeValueAsString(requestJson);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return RouterRequest.builder()
                .operation(REFUND_OPERATION)
                .requestJson(requestJsonAsString).build();
    }
}