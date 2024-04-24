package com.mgm.pd.cp.resortpayment.util.capture;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mgm.pd.cp.payment.common.constant.BooleanValue;
import com.mgm.pd.cp.payment.common.constant.OrderType;
import com.mgm.pd.cp.payment.common.dto.CPRequestHeaders;
import com.mgm.pd.cp.payment.common.dto.opera.Card;
import com.mgm.pd.cp.payment.common.dto.opera.DetailedAmount;
import com.mgm.pd.cp.payment.common.dto.opera.TransactionAmount;
import com.mgm.pd.cp.resortpayment.dto.capture.CPPaymentCaptureRequest;
import com.mgm.pd.cp.resortpayment.dto.capture.CaptureRouterRequestJson;
import com.mgm.pd.cp.resortpayment.dto.common.BaseTransactionDetails;
import com.mgm.pd.cp.resortpayment.dto.common.Customer;
import com.mgm.pd.cp.resortpayment.dto.common.SaleItem;
import com.mgm.pd.cp.resortpayment.dto.common.TransactionDetails;
import com.mgm.pd.cp.resortpayment.dto.router.RouterRequest;
import com.mgm.pd.cp.resortpayment.util.common.PaymentProcessingServiceHelper;
import lombok.AllArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Objects;

import static com.mgm.pd.cp.payment.common.constant.ApplicationConstants.*;

/**
 * This class is responsible for taking a class and converting it to RouterRequest compatible
 */
@Component
@AllArgsConstructor
public class CaptureToRouterConverter implements Converter<CPPaymentCaptureRequest, RouterRequest> {
    private ObjectMapper mapper;
    private PaymentProcessingServiceHelper helper;

    @Override
    public RouterRequest convert(CPPaymentCaptureRequest source) {
        BaseTransactionDetails baseTransactionDetails = helper.getBaseTransactionDetails(source);
        SaleItem saleItem = Objects.nonNull(baseTransactionDetails.getSaleItem()) ? baseTransactionDetails.getSaleItem() : new SaleItem();
        String saleType = saleItem.getSaleType();
        saleType = Objects.nonNull(saleType) ? saleType: "";
        HashMap<String, String> valueFromSaleDetails = Objects.nonNull(helper.getSaleDetailsObject(baseTransactionDetails)) ? helper.getSaleDetailsObject(baseTransactionDetails) : new HashMap<>();
        TransactionDetails transactionDetails = source.getTransactionDetails();
        TransactionAmount transactionAmount = transactionDetails.getTransactionAmount();
        DetailedAmount detailedAmount = Objects.nonNull(transactionAmount.getDetailedAmount()) ? transactionAmount.getDetailedAmount() : new DetailedAmount();
        Card card = transactionDetails.getCard();
        Customer customer = Objects.nonNull(transactionDetails.getCustomer()) ? transactionDetails.getCustomer() : new Customer();
        CPRequestHeaders headers = source.getHeaders();
        String originalTransactionIdentifier = source.getOriginalTransactionIdentifier();
        Boolean isCardPresent = Objects.nonNull(transactionDetails.getIsCardPresent()) ? transactionDetails.getIsCardPresent() : Boolean.TRUE;
        CaptureRouterRequestJson requestJson = CaptureRouterRequestJson.builder()
                .amount(detailedAmount.getAmount())
                .taxAmount(detailedAmount.getTax())
                .totalAuthAmount(transactionAmount.getRequestedAmount())
                .departureDate(valueFromSaleDetails.get(CHECK_OUT_DATE))
                .arrivalDate(valueFromSaleDetails.get(CHECK_IN_DATE))
                .messageType(String.valueOf(source.getTransactionType()))
                .guestName(customer.getFullName())
                .cardNumber(card.getTokenValue())
                .cardExpirationDate(card.getExpiryDate())
                .cardPresent(BooleanValue.getEnumByString(isCardPresent.toString()))
                .resvNameID(saleItem.getSaleReferenceIdentifier())
                .roomNum(saleType.equals(OrderType.Hotel.name()) ? valueFromSaleDetails.get(ROOM_NUMBER) : valueFromSaleDetails.get(TICKET_NUMBER))
                .vendorTranID(source.getTransactionAuthChainId())
                .sequenceNumber(source.getTransactionIdentifier())
                .originalAuthSequence(Objects.nonNull(originalTransactionIdentifier) ? Long.valueOf(originalTransactionIdentifier) : null)
                .transDate(source.getTransactionDateTime())
                .clientID(headers.getClientId())
                .corelationId(headers.getCorrelationId())
                .build();
        String requestJsonAsString;
        try {
            requestJsonAsString = mapper.writeValueAsString(requestJson);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return RouterRequest.builder()
                .operation(CAPTURE_OPERATION)
                //.gatewayId(String.valueOf(initialPayment.getGatewayId()))
                .requestJson(requestJsonAsString).build();
    }
}
