package com.mgm.pd.cp.resortpayment.util.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mgm.pd.cp.payment.common.constant.AuthType;
import com.mgm.pd.cp.payment.common.dto.CPRequestHeaders;
import com.mgm.pd.cp.payment.common.dto.GenericResponse;
import com.mgm.pd.cp.payment.common.dto.opera.OperaResponse;
import com.mgm.pd.cp.payment.common.model.Payment;
import com.mgm.pd.cp.resortpayment.dto.CPPaymentProcessingRequest;
import com.mgm.pd.cp.resortpayment.dto.cardvoid.CPPaymentCardVoidRequest;
import com.mgm.pd.cp.resortpayment.dto.common.BaseTransactionDetails;
import com.mgm.pd.cp.resortpayment.dto.common.SaleItem;
import com.mgm.pd.cp.resortpayment.exception.MissingHeaderException;
import com.mgm.pd.cp.resortpayment.service.payment.FindPaymentService;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import javax.validation.Valid;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Helper class for utility methods
 */
@Component
@AllArgsConstructor
public class PaymentProcessingServiceHelper {
    private static final Logger logger = LogManager.getLogger(PaymentProcessingServiceHelper.class);
    private FindPaymentService findPaymentService;
    private ObjectMapper mapper;
    private Converter converter;

    /**
     * This method is helping to find the complete sale details object at once from the request
     *
     * @param transactionDetails: to fetch SaleDetails Object
     */
    public LinkedHashMap<String, String> getSaleDetailsObject(BaseTransactionDetails transactionDetails) {
        if (Objects.nonNull(transactionDetails)) {
            SaleItem<?> saleItem = transactionDetails.getSaleItem();
            if (Objects.nonNull(saleItem)) {
                Object saleDetails = saleItem.getSaleDetails();
                if (Objects.nonNull(saleDetails) && saleDetails instanceof LinkedHashMap) {
                    return (LinkedHashMap<String, String>) saleDetails;
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
        return response(operaResponse);
    }

    //internal method to add complete data in the response payload
    private <D> ResponseEntity<GenericResponse<?>> response(D data) {
        return new ResponseEntity<>(GenericResponse.builder().data(data).build(), HttpStatus.OK);
    }

    /**
     * This method is responsible to find and return initial Authorization Payment for all types of requests.
     * @param request: any type of Card Payment Request
     * @return Payment details from Payment DB
     */
    public <T> Optional<Payment> getInitialAuthPayment(T request) {
        long authChainId;
        Optional<List<Payment>> paymentDetails;
        if (request.getClass().equals(CPPaymentCardVoidRequest.class)) {
            authChainId = Long.parseLong(((CPPaymentCardVoidRequest) request).getAuthChainId());
            paymentDetails = findPaymentService.getPaymentDetails(authChainId);
        } else {
            authChainId = Long.parseLong(((CPPaymentProcessingRequest) request).getAuthChainId());
            @Valid AuthType transactionType = ((CPPaymentProcessingRequest) request).getTransactionType();
            if (transactionType == AuthType.DEPOSIT) {
                paymentDetails = findPaymentService.getPaymentDetails(authChainId, transactionType);
            } else {
                paymentDetails = findPaymentService.getPaymentDetails(authChainId);
            }
        }
        if (paymentDetails.isPresent()) {
            List<Payment> payments = paymentDetails.get();
            if (!payments.isEmpty()) {
                return Optional.ofNullable(payments.get(payments.size() - 1));
            }
        }
        logger.log(Level.WARN, "Parent Payment transaction is missing in Payment DB for authChainId: " + authChainId);
        return Optional.empty();
    }

    /**
     * This method is helping to find the complete TransactionDetails object at once from the request
     *
     * @param request: generic for all types of request
     */
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
     * This method checks for all required Headers in the
     * request and maps it to the CPRequestHeaders class
     * @param request: all types of requests are accepted
     * @param headers: Request Headers
     */
    public <T> T mapHeadersInRequest(T request, HttpHeaders headers) {
        headers.remove("Content-Length");
        throwExceptionIfHeadersAreMissing(headers);
        return addHeadersInRequest(request, headers);
    }

    /**
     * This method is adding headers in the request
     * @param request: request
     * @param headers: headers in the payload
     */
    private <T> T addHeadersInRequest(T request, HttpHeaders headers) {
        CPRequestHeaders cpRequestHeaders = mapper.convertValue(headers.toSingleValueMap(), CPRequestHeaders.class);
        if (request.getClass().equals(CPPaymentCardVoidRequest.class)) {
            ((CPPaymentCardVoidRequest) request).setHeaders(cpRequestHeaders);
        } else {
            ((CPPaymentProcessingRequest) request).setHeaders(cpRequestHeaders);
        }
        return request;
    }

    //method used to throw exception to the caller if Headers are missing
    private static void throwExceptionIfHeadersAreMissing(HttpHeaders headers) {
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
    }

    //used for converting to compatible date format
    public LocalDateTime convertToTimestamp(String transactionDateTime) {
        transactionDateTime = transactionDateTime.substring(0, 19);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd['T']HH:mm:ss['Z']");
        return LocalDateTime.parse(transactionDateTime, formatter);
    }
}
