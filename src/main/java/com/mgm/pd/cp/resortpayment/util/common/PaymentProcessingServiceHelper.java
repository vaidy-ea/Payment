package com.mgm.pd.cp.resortpayment.util.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mgm.pd.cp.payment.common.dto.CPRequestHeaders;
import com.mgm.pd.cp.payment.common.dto.ErrorResponse;
import com.mgm.pd.cp.payment.common.dto.GenericResponse;
import com.mgm.pd.cp.payment.common.dto.opera.OperaResponse;
import com.mgm.pd.cp.payment.common.model.Payment;
import com.mgm.pd.cp.resortpayment.dto.BaseTransactionDetails;
import com.mgm.pd.cp.resortpayment.dto.CPPaymentProcessingRequest;
import com.mgm.pd.cp.resortpayment.dto.SaleItem;
import com.mgm.pd.cp.resortpayment.dto.cardvoid.CPPaymentCardVoidRequest;
import com.mgm.pd.cp.resortpayment.exception.MissingHeaderException;
import com.mgm.pd.cp.resortpayment.service.payment.FindPaymentService;
import feign.FeignException;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

import static com.mgm.pd.cp.payment.common.constant.ApplicationConstants.INITIAL_PAYMENT_IS_MISSING;
import static com.mgm.pd.cp.payment.common.constant.ApplicationConstants.INTELLIGENT_ROUTER_CONNECTION_EXCEPTION_MESSAGE;

/**
 * Helper class for utility methods
 */
@Component
@AllArgsConstructor
public class PaymentProcessingServiceHelper {
    private FindPaymentService findPaymentService;
    private ObjectMapper mapper;
    private Converter converter;

    /**
     *
     * @param feignEx: taking exception from Intelligent Router
     * returning details to add in comments column
     */
    public String getCommentsFromException(FeignException feignEx) throws JsonProcessingException {
        String contentedUTF8 = feignEx.contentUTF8();
        if (!contentedUTF8.isBlank()) {
            ErrorResponse irEx = mapper.readValue(contentedUTF8, ErrorResponse.class);
            //To get the error message from Router to save in comments column in Payment table
            return irEx.getDetail();
        }
        return INTELLIGENT_ROUTER_CONNECTION_EXCEPTION_MESSAGE;
    }

    public <T> String getValueFromSaleDetails(BaseTransactionDetails transactionDetails, String key) {
        //BaseTransactionDetails transactionDetails = getBaseTransactionDetails(request);
        if (Objects.nonNull(transactionDetails)) {
            SaleItem<?> saleItem = transactionDetails.getSaleItem();
            if (Objects.nonNull(saleItem)) {
                Object saleDetails = saleItem.getSaleDetails();
                if (Objects.nonNull(saleDetails) && saleDetails instanceof LinkedHashMap) {
                    return String.valueOf(((LinkedHashMap<?, ?>) saleDetails).get(key));
                }
            }
        }
        return null;
    }

    /**
     * Method to convert a positive response from Payment DB for Opera
     * @param payment: data from Payment DB
     */
    public ResponseEntity<GenericResponse<?>> response(Payment payment) {
        OperaResponse operaResponse;
        //converting the response from Payment DB for Opera
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
        Long authChainId;
        String transactionType;
        Optional<List<Payment>> paymentDetails;
        if (request.getClass().equals(CPPaymentCardVoidRequest.class)) {
            authChainId = ((CPPaymentCardVoidRequest) request).getAuthChainId();
            paymentDetails = findPaymentService.getPaymentDetails(authChainId);
        } else {
            authChainId = ((CPPaymentProcessingRequest) request).getAuthChainId();
            transactionType = ((CPPaymentProcessingRequest) request).getTransactionType().name();
            paymentDetails = findPaymentService.getPaymentDetails(authChainId, transactionType);
        }
        if (paymentDetails.isPresent()) {
            List<Payment> payments = paymentDetails.get();
            List<Payment> approvedPayments = payments.stream()
                    .filter(ts -> (Objects.nonNull(ts.getTransactionStatus()) && ts.getTransactionStatus().equals("Success")))
                    .filter(grc -> (Objects.nonNull(grc.getGatewayResponseCode()) && grc.getGatewayResponseCode().equals("Approved")))
                    .collect(Collectors.toList());
            if (!approvedPayments.isEmpty()) {
                return Optional.ofNullable(approvedPayments.get(approvedPayments.size() - 1));
            }
        }
        return Optional.empty();
    }

    public <T> BaseTransactionDetails getBaseTransactionDetails(T request) {
        BaseTransactionDetails transactionDetails;
        if (request.getClass().equals(CPPaymentCardVoidRequest.class)) {
            transactionDetails = ((CPPaymentCardVoidRequest) request).getTransactionDetails();
        } else {
            transactionDetails = ((CPPaymentProcessingRequest) request).getTransactionDetails();
        }
        return transactionDetails;
    }

    /**
     * This method check for all required Headers in the
     * request and maps it to the CustomHeader class
     * @param request: all types of requests are accepted
     * @param headers: Request Headers
     */
    public <T> T mapHeadersInRequest(T request, HttpHeaders headers) {
        List<String> missingHeaders = new ArrayList<>();
        for (Field f: CPRequestHeaders.class.getDeclaredFields()) {
            String value = f.getAnnotation(JsonProperty.class).value();
            if (!headers.containsKey(value)) {
                missingHeaders.add(value);
            }
        }
        if (!missingHeaders.isEmpty()) {
            throw new MissingHeaderException(missingHeaders);
        }
        CPRequestHeaders cpRequestHeaders = mapper.convertValue(headers.toSingleValueMap(), CPRequestHeaders.class);
        if (request.getClass().equals(CPPaymentCardVoidRequest.class)) {
            ((CPPaymentCardVoidRequest) request).setHeaders(cpRequestHeaders);
        } else {
            ((CPPaymentProcessingRequest) request).setHeaders(cpRequestHeaders);
        }
        headers.remove("Content-Length");
        return request;
    }
}
