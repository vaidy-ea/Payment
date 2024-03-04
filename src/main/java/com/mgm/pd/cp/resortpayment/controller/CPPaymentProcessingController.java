package com.mgm.pd.cp.resortpayment.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mgm.pd.cp.payment.common.dto.GenericResponse;
import com.mgm.pd.cp.resortpayment.dto.authorize.CPPaymentAuthorizationRequest;
import com.mgm.pd.cp.resortpayment.dto.capture.CPPaymentCaptureRequest;
import com.mgm.pd.cp.resortpayment.dto.cardvoid.CPPaymentCardVoidRequest;
import com.mgm.pd.cp.resortpayment.dto.incrementalauth.CPPaymentIncrementalAuthRequest;
import com.mgm.pd.cp.resortpayment.dto.refund.CPPaymentRefundRequest;
import com.mgm.pd.cp.resortpayment.service.payment.CpPaymentProcessingService;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * This clas is responsible for handling multiple operations
 * related to Card Payment Processing like Authorization,
 * Capture, Refund, Void etc.
 * All methods are responsible for Validating the request.
 */
@RestController
@RequestMapping("/services/v1/payments")
@AllArgsConstructor
public class CPPaymentProcessingController {
    private static final Logger logger = LogManager.getLogger(CPPaymentProcessingController.class);
    private CpPaymentProcessingService cpPaymentProcessingService;

    /**
     * This method is responsible for handling request
     * for Incremental Authorization.
     *
     * @param request: needs a valid request to process the complete process
     * @return response for Opera
     */
    @PostMapping("/incrementalauth")
    public ResponseEntity<GenericResponse<?>> incrementalAuth(@RequestHeader HttpHeaders headers, @Valid @RequestBody CPPaymentIncrementalAuthRequest request) throws JsonProcessingException {
        logger.log(Level.DEBUG, "incrementalAuth Request in DEBUG is : " + request.getTransactionType());
        return processPayload(request, headers);
    }

    /**
     * This method is responsible for handling request
     * for Authorization.
     *
     * @param cpPaymentAuthorizationRequest: needs a valid request to process the complete process
     * @return response for Opera
     */
    @PostMapping("/authorize")
    public ResponseEntity<GenericResponse<?>> authorize(@RequestHeader HttpHeaders headers, @Valid @RequestBody CPPaymentAuthorizationRequest cpPaymentAuthorizationRequest) throws JsonProcessingException {
        logger.log(Level.DEBUG, "authorize Request in DEBUG is : {} ", cpPaymentAuthorizationRequest.getTransactionType());
        return processPayload(cpPaymentAuthorizationRequest, headers);
    }

    /**
     * This method is responsible for handling request
     * for Capture Operation.
     *
     * @param cpPaymentCaptureRequest: needs a valid request to process the complete process
     * @return response for Opera
     */
    @PostMapping("/capture")
    public ResponseEntity<GenericResponse<?>> capture(@RequestHeader HttpHeaders headers, @Valid @RequestBody CPPaymentCaptureRequest cpPaymentCaptureRequest) throws JsonProcessingException {
        logger.log(Level.DEBUG, "capture Request in DEBUG is : " + cpPaymentCaptureRequest.getTransactionType());
        return processPayload(cpPaymentCaptureRequest, headers);
    }

    /**
     * This method is responsible for handling request
     * for cardVoid operation.
     * @param cpPaymentCardVoidRequest: needs a valid request to process the complete process
     * @return response for Opera
     */
    @PostMapping("/void")
    public ResponseEntity<GenericResponse<?>> cardVoid(@RequestHeader HttpHeaders headers, @Valid @RequestBody CPPaymentCardVoidRequest cpPaymentCardVoidRequest) throws JsonProcessingException {
        logger.log(Level.DEBUG, "cardVoid Request in DEBUG is : " + cpPaymentCardVoidRequest.getTransactionIdentifier());
        return processPayload(cpPaymentCardVoidRequest, headers);
    }

    /**
     * This method is responsible for handling request
     * for Refund operation.
     * @param cpPaymentRefundRequest: needs a valid request to process the complete process
     * @return response for Opera
     */
    @PostMapping("/refund")
    public ResponseEntity<GenericResponse<?>> refund(@RequestHeader HttpHeaders headers, @Valid @RequestBody CPPaymentRefundRequest cpPaymentRefundRequest) throws JsonProcessingException {
        logger.log(Level.DEBUG, "refund Request in DEBUG is : " + cpPaymentRefundRequest.getTransactionType());
        return processPayload(cpPaymentRefundRequest, headers);
    }

    /**
     * This method takes the valid request for Incremental Authorization
     * and pass it to cpPaymentProcessingService to process.
     */
    private ResponseEntity<GenericResponse<?>> processPayload(CPPaymentIncrementalAuthRequest request, HttpHeaders headers) throws JsonProcessingException {
        return cpPaymentProcessingService.processIncrementalAuthorizationRequest(request, headers);
    }

    /**
     * This method takes the valid request for Authorization
     * and pass it to cpPaymentProcessingService to process.
     */
    private ResponseEntity<GenericResponse<?>> processPayload(CPPaymentAuthorizationRequest cpPaymentAuthorizationRequest, HttpHeaders headers) throws JsonProcessingException {
        return cpPaymentProcessingService.processAuthorizeRequest(cpPaymentAuthorizationRequest, headers);
    }

    /**
     * This method takes the valid request for Capture
     * and pass it to cpPaymentProcessingService to process.
     */
    private ResponseEntity<GenericResponse<?>> processPayload(CPPaymentCaptureRequest cpPaymentCaptureRequest, HttpHeaders headers) throws JsonProcessingException {
        return cpPaymentProcessingService.processCaptureRequest(cpPaymentCaptureRequest, headers);
    }

    /**
     * This method takes the valid request for CardVoid
     * and pass it to cpPaymentProcessingService to process.
     */
    private ResponseEntity<GenericResponse<?>> processPayload(CPPaymentCardVoidRequest cpPaymentCardVoidRequest, HttpHeaders headers) throws JsonProcessingException {
        return cpPaymentProcessingService.processCardVoidRequest(cpPaymentCardVoidRequest, headers);
    }

    /**
     * This method takes the valid request for Refund
     * and pass it to cpPaymentProcessingService to process.
     */
    private ResponseEntity<GenericResponse<?>> processPayload(CPPaymentRefundRequest cpPaymentRefundRequest, HttpHeaders headers) throws JsonProcessingException {
        return cpPaymentProcessingService.processRefundRequest(cpPaymentRefundRequest, headers);
    }
}
