package com.mgm.pd.cp.resortpayment.util.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mgm.pd.cp.payment.common.dto.ErrorResponse;
import com.mgm.pd.cp.payment.common.dto.GenericResponse;
import com.mgm.pd.cp.payment.common.dto.opera.OperaResponse;
import com.mgm.pd.cp.payment.common.model.Payment;
import com.mgm.pd.cp.resortpayment.dto.BaseTransactionDetails;
import com.mgm.pd.cp.resortpayment.dto.CPPaymentProcessingRequest;
import com.mgm.pd.cp.resortpayment.dto.SaleItem;
import com.mgm.pd.cp.resortpayment.dto.cardvoid.CPPaymentCardVoidRequest;
import com.mgm.pd.cp.resortpayment.service.payment.FindPaymentService;
import feign.FeignException;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.Optional;

import static com.mgm.pd.cp.payment.common.constant.ApplicationConstants.*;

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

    public <T> String getValueFromSaleDetails(T request, String key) {
        BaseTransactionDetails transactionDetails = getBaseTransactionDetails(request);
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
        BaseTransactionDetails transactionDetails = getBaseTransactionDetails(request);
        if (Objects.nonNull(transactionDetails)) {
            SaleItem<?> saleItem = transactionDetails.getSaleItem();
            //Property Identifies is equivalent to PropertyCode and SaleReferenceIdentifier is equivalent to ResortId
            return findPaymentService.getPaymentDetails(getValueFromSaleDetails(request, PROPERTY_IDENTIFIER),
                    ((Objects.nonNull(saleItem) && Objects.nonNull(saleItem.getSaleDetails())) ? saleItem.getSaleReferenceIdentifier() : null));
        }
        return Optional.empty();
    }

    private static <T> BaseTransactionDetails getBaseTransactionDetails(T request) {
        BaseTransactionDetails transactionDetails;
        if (request.getClass().equals(CPPaymentCardVoidRequest.class)) {
            transactionDetails = ((CPPaymentCardVoidRequest) request).getTransactionDetails();
        } else {
            transactionDetails = ((CPPaymentProcessingRequest) request).getTransactionDetails();
        }
        return transactionDetails;
    }
}
