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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<GenericResponse<?>> incrementalAuth(@Valid @RequestBody CPPaymentIncrementalAuthRequest request) throws JsonProcessingException {
        logger.log(Level.DEBUG, "incrementalAuth Request in DEBUG is : " + request.getTransactionType());
        return processPayload(request);
    }

    /**
     * This method is responsible for handling request
     * for Authorization.
     *
     * @param cpPaymentAuthorizationRequest: needs a valid request to process the complete process
     * @return response for Opera
     */
    @PostMapping("/authorize")
    public ResponseEntity<GenericResponse<?>> authorize(@Valid @RequestBody CPPaymentAuthorizationRequest cpPaymentAuthorizationRequest) throws JsonProcessingException {
        logger.log(Level.DEBUG, "authorize Request in DEBUG is : {} ", cpPaymentAuthorizationRequest.getTransactionType());
        return processPayload(cpPaymentAuthorizationRequest);

    }

    /**
     * This method is responsible for handling request
     * for Capture Operation.
     *
     * @param cpPaymentCaptureRequest: needs a valid request to process the complete process
     * @return response for Opera
     */
    @PostMapping("/capture")
    public ResponseEntity<GenericResponse<?>> capture(@Valid @RequestBody CPPaymentCaptureRequest cpPaymentCaptureRequest) throws JsonProcessingException {
        logger.log(Level.DEBUG, "capture Request in DEBUG is : " + cpPaymentCaptureRequest.getTransactionType());
        return processPayload(cpPaymentCaptureRequest);
    }

    /**
     * This method is responsible for handling request
     * for cardVoid operation.
     * @param cpPaymentCardVoidRequest: needs a valid request to process the complete process
     * @return response for Opera
     */
    @PostMapping("/void")
    public ResponseEntity<GenericResponse<?>> cardVoid(@Valid @RequestBody CPPaymentCardVoidRequest cpPaymentCardVoidRequest) throws JsonProcessingException {
        logger.log(Level.DEBUG, "cardVoid Request in DEBUG is : " + cpPaymentCardVoidRequest.getPropertyCode());
        return processPayload(cpPaymentCardVoidRequest);
    }

    /**
     * This method is responsible for handling request
     * for Refund operation.
     * @param cpPaymentRefundRequest: needs a valid request to process the complete process
     * @return response for Opera
     */
    @PostMapping("/refund")
    public ResponseEntity<GenericResponse<?>> refund(@Valid @RequestBody CPPaymentRefundRequest cpPaymentRefundRequest) throws JsonProcessingException {
        logger.log(Level.DEBUG, "refund Request in DEBUG is : " + cpPaymentRefundRequest.getTransactionType());
        return processPayload(cpPaymentRefundRequest);
    }

    /**
     * This method takes the valid request for Incremental Authorization
     * and pass it to cpPaymentProcessingService to process.
     */
    private ResponseEntity<GenericResponse<?>> processPayload(CPPaymentIncrementalAuthRequest request) throws JsonProcessingException {
        return cpPaymentProcessingService.processIncrementalAuthorizationRequest(request);
    }

    /**
     * This method takes the valid request for Authorization
     * and pass it to cpPaymentProcessingService to process.
     */
    private ResponseEntity<GenericResponse<?>> processPayload(CPPaymentAuthorizationRequest cpPaymentAuthorizationRequest) throws JsonProcessingException {
        return cpPaymentProcessingService.processAuthorizeRequest(cpPaymentAuthorizationRequest);
    }

    /**
     * This method takes the valid request for Capture
     * and pass it to cpPaymentProcessingService to process.
     */
    private ResponseEntity<GenericResponse<?>> processPayload(CPPaymentCaptureRequest cpPaymentCaptureRequest) throws JsonProcessingException {
        return cpPaymentProcessingService.processCaptureRequest(cpPaymentCaptureRequest);
    }

    /**
     * This method takes the valid request for CardVoid
     * and pass it to cpPaymentProcessingService to process.
     */
    private ResponseEntity<GenericResponse<?>> processPayload(CPPaymentCardVoidRequest cpPaymentCardVoidRequest) throws JsonProcessingException {
        return cpPaymentProcessingService.processCardVoidRequest(cpPaymentCardVoidRequest);
    }

    /**
     * This method takes the valid request for Refund
     * and pass it to cpPaymentProcessingService to process.
     */
    private ResponseEntity<GenericResponse<?>> processPayload(CPPaymentRefundRequest cpPaymentRefundRequest) throws JsonProcessingException {
        return cpPaymentProcessingService.processRefundRequest(cpPaymentRefundRequest);
    }
}
