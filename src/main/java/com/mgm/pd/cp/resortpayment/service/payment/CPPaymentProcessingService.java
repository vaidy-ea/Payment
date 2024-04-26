package com.mgm.pd.cp.resortpayment.service.payment;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.mgm.pd.cp.payment.common.dto.GenericResponse;
import com.mgm.pd.cp.resortpayment.dto.authorize.CPPaymentAuthorizationRequest;
import com.mgm.pd.cp.resortpayment.dto.capture.CPPaymentCaptureRequest;
import com.mgm.pd.cp.resortpayment.dto.cardvoid.CPPaymentCardVoidRequest;
import com.mgm.pd.cp.resortpayment.dto.incrementalauth.CPPaymentIncrementalAuthRequest;
import com.mgm.pd.cp.resortpayment.dto.refund.CPPaymentRefundRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

/**
 *  This interface provides methods/contract for Card Payment Operations
 *  This class has only one Implementation Class(CpPaymentProcessingServiceImpl.java)
 *  It is responsible to define the request and response type for each of the operation.
 */
public interface CPPaymentProcessingService {
    /**
     * This method takes the action for Authorization operation only.
     * It shouldn't be called without passing the Authorization Request.
     */
    ResponseEntity<GenericResponse> processAuthorizeRequest(CPPaymentAuthorizationRequest request, HttpHeaders headers) throws JsonProcessingException;

    /**
     * This method takes the action for IncrementalAuthorization operation only.
     * It shouldn't be called without passing the IncrementalAuthorization Request.
     */
    ResponseEntity<GenericResponse> processIncrementalAuthorizationRequest(CPPaymentIncrementalAuthRequest request, HttpHeaders headers) throws JsonProcessingException;

    /**
     * This method takes the action for Capture operation only.
     * It shouldn't be called without passing the Capture Request.
     */
    ResponseEntity<GenericResponse> processCaptureRequest(CPPaymentCaptureRequest request, HttpHeaders headers) throws JsonProcessingException;

    /**
     * This method takes the action for Card Void operation only.
     * It shouldn't be called without passing the Card Void Request.
     */
    ResponseEntity<GenericResponse> processCardVoidRequest(CPPaymentCardVoidRequest request, HttpHeaders headers) throws JsonProcessingException;

    /**
     * This method takes the action for Refund operation only.
     * It shouldn't be called without passing the Refund Request.
     */
    ResponseEntity<GenericResponse> processRefundRequest(CPPaymentRefundRequest request, HttpHeaders headers) throws JsonProcessingException;
}
