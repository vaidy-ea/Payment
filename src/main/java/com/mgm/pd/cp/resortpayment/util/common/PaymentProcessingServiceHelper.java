package com.mgm.pd.cp.resortpayment.util.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mgm.pd.cp.payment.common.dto.ErrorResponse;
import com.mgm.pd.cp.payment.common.dto.GenericResponse;
import com.mgm.pd.cp.payment.common.dto.opera.OperaResponse;
import com.mgm.pd.cp.payment.common.model.Payment;
import com.mgm.pd.cp.resortpayment.dto.CPPaymentProcessingRequest;
import com.mgm.pd.cp.resortpayment.dto.SaleItem;
import com.mgm.pd.cp.resortpayment.dto.cardvoid.CardVoidRouterResponse;
import com.mgm.pd.cp.resortpayment.service.payment.FindPaymentService;
import feign.FeignException;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.Optional;

import static com.mgm.pd.cp.payment.common.constant.ApplicationConstants.INITIAL_PAYMENT_IS_MISSING;
import static com.mgm.pd.cp.payment.common.constant.ApplicationConstants.INTELLIGENT_ROUTER_CONNECTION_EXCEPTION_MESSAGE;

@Component
@AllArgsConstructor
public class PaymentProcessingServiceHelper {
    public static final String PROPERTY_IDENTIFIER = "propertyIdentifier";
    private FindPaymentService findPaymentService;
    private ObjectMapper mapper;
    private Converter converter;

    public String getCommentsFromException(FeignException feignEx) throws JsonProcessingException {
        String contentedUTF8 = feignEx.contentUTF8();
        if (!contentedUTF8.isBlank()) {
            ErrorResponse irEx = mapper.readValue(contentedUTF8, ErrorResponse.class);
            //To get the error message from Router to save in comments column in Payment table
            return irEx.getDetail();
        }
        return INTELLIGENT_ROUTER_CONNECTION_EXCEPTION_MESSAGE;
    }

    public CardVoidRouterResponse addCommentsForCardVoidResponse(FeignException feignEx) throws JsonProcessingException {
        String contentedUTF8 = feignEx.contentUTF8();
        CardVoidRouterResponse cvrResponse;
        if (!contentedUTF8.isBlank()) {
            ErrorResponse irEx = mapper.readValue(contentedUTF8, ErrorResponse.class);
            //To get the error message from Router to save in comments column in Payment table
            cvrResponse = CardVoidRouterResponse.builder().comments(irEx.getDetail()).build();
        } else {
            cvrResponse = CardVoidRouterResponse.builder().comments(INTELLIGENT_ROUTER_CONNECTION_EXCEPTION_MESSAGE).build();
        }
        return cvrResponse;
    }

    public <T> String getValueFromSaleDetails(T request, String key) {
        SaleItem saleItem = ((CPPaymentProcessingRequest) request).getTransactionDetails().getSaleItem();
        if (Objects.nonNull(saleItem)) {
            Object saleDetails = saleItem.getSaleDetails();
            if (Objects.nonNull(saleDetails) && saleDetails instanceof LinkedHashMap) {
                return String.valueOf(((LinkedHashMap<?, ?>) saleDetails).get(key));
            }
        }
        return null;
    }

    public ResponseEntity<GenericResponse<?>> response(Payment payment) {
        OperaResponse operaResponse;
        operaResponse = converter.convert(payment);
        return response(operaResponse, HttpStatus.OK);
    }

    private <D> ResponseEntity<GenericResponse<?>> response(D data, HttpStatus status) {
        return new ResponseEntity<>(GenericResponse.builder().data(data).build(), status);
    }

    public ResponseEntity<GenericResponse<?>> initialPaymentIsMissing() {
        return response(new ErrorResponse(null, 422, INITIAL_PAYMENT_IS_MISSING,
                INITIAL_PAYMENT_IS_MISSING, null, null, null), HttpStatus.UNPROCESSABLE_ENTITY);
    }

    /**
     * This method is responsible to find and return initial Authorization Payment for all types of requests.
     * @param request: any type of Card Payment Request
     * @return Payment details from Payment DB
     */
    public <T> Optional<Payment> getInitialAuthPayment(T request) {
        SaleItem<?> saleItem = ((CPPaymentProcessingRequest) request).getTransactionDetails().getSaleItem();
        return findPaymentService.getPaymentDetails(getValueFromSaleDetails(request, PROPERTY_IDENTIFIER),
                ((Objects.nonNull(saleItem) && Objects.nonNull(saleItem.getSaleDetails())) ? saleItem.getSaleReferenceIdentifier() : null));
    }
}
