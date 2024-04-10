package com.mgm.pd.cp.resortpayment.util.cardvoid;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mgm.pd.cp.payment.common.constant.OrderType;
import com.mgm.pd.cp.payment.common.dto.CPRequestHeaders;
import com.mgm.pd.cp.payment.common.dto.opera.Card;
import com.mgm.pd.cp.resortpayment.dto.cardvoid.CPPaymentCardVoidRequest;
import com.mgm.pd.cp.resortpayment.dto.cardvoid.CardVoidRouterRequestJson;
import com.mgm.pd.cp.resortpayment.dto.common.BaseTransactionDetails;
import com.mgm.pd.cp.resortpayment.dto.common.SaleItem;
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
public class VoidToRouterConverter implements Converter<CPPaymentCardVoidRequest, RouterRequest> {
    private ObjectMapper mapper;
    private PaymentProcessingServiceHelper helper;

    @Override
    public RouterRequest convert(CPPaymentCardVoidRequest source) {
        BaseTransactionDetails baseTransactionDetails = Objects.nonNull(helper.getBaseTransactionDetails(source)) ? helper.getBaseTransactionDetails(source) : new BaseTransactionDetails();
        SaleItem saleItem = Objects.nonNull(baseTransactionDetails.getSaleItem()) ? baseTransactionDetails.getSaleItem() : new SaleItem();
        String saleType = saleItem.getSaleType();
        saleType = Objects.nonNull(saleType) ? saleType: "";
        HashMap<String, String> valueFromSaleDetails = Objects.nonNull(helper.getSaleDetailsObject(baseTransactionDetails)) ? helper.getSaleDetailsObject(baseTransactionDetails) : new HashMap<>();
        BaseTransactionDetails transactionDetails = Objects.nonNull(source.getTransactionDetails()) ? source.getTransactionDetails() : new BaseTransactionDetails();
        Card card = Objects.nonNull(transactionDetails.getCard()) ? transactionDetails.getCard() : new Card();
        CPRequestHeaders headers = source.getHeaders();
        String originalTransactionIdentifier = source.getOriginalTransactionIdentifier();
        CardVoidRouterRequestJson requestJson = CardVoidRouterRequestJson.builder()
                .departureDate(valueFromSaleDetails.get(CHECK_OUT_DATE))
                .arrivalDate(valueFromSaleDetails.get(CHECK_IN_DATE))
                .cardNumber(card.getTokenValue())
                .cardExpirationDate(card.getExpiryDate())
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
                .operation(VOID_OPERATION)
                .gatewayId(SHIFT4_GATEWAY_ID)
                .requestJson(requestJsonAsString).build();
    }
}
