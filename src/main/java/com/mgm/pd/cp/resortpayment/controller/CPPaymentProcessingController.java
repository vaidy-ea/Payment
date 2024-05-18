package com.mgm.pd.cp.resortpayment.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mgm.pd.cp.payment.common.audit.service.AuditEventProducer;
import com.mgm.pd.cp.payment.common.dto.*;
import com.mgm.pd.cp.resortpayment.service.payment.CPPaymentProcessingService;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.text.ParseException;
import java.util.Map;

import static com.mgm.pd.cp.payment.common.audit.constant.AuditConstant.*;

/**
 * This class is responsible for handling multiple operations
 * related to Card Payment Processing like Authorization,
 * Capture, Refund, Void etc.
 * All methods are responsible for Validating the request.
 */
@RestController
@RequestMapping("/services/paymentprocess/v1")
@AllArgsConstructor
public class CPPaymentProcessingController {
    private static final Logger logger = LogManager.getLogger(CPPaymentProcessingController.class);
    private CPPaymentProcessingService cpPaymentProcessingService;
    private AuditEventProducer auditEventProducer;

    /**
     * This method is responsible for handling request
     * for Authorization.
     *
     * @param cpPaymentAuthorizationRequest: needs a valid request to process
     * @return response for Opera
     */
    @PostMapping("/authorize")
    public ResponseEntity<GenericResponse> authorize(@RequestHeader HttpHeaders headers, @Valid @RequestBody CPPaymentAuthorizationRequest cpPaymentAuthorizationRequest) throws JsonProcessingException, ParseException {
        logger.log(Level.INFO, "Authorize Request is: {}", cpPaymentAuthorizationRequest);
        return processPayload(cpPaymentAuthorizationRequest, headers);
    }

    /**
     * This method is responsible for handling request
     * for Incremental Authorization.
     *
     * @param request: needs a valid request to process
     * @return response for Opera
     */
    @PostMapping("/authorize/incremental")
    public ResponseEntity<GenericResponse> incrementalAuth(@RequestHeader HttpHeaders headers, @Valid @RequestBody CPPaymentIncrementalAuthRequest request) throws JsonProcessingException, ParseException {
        logger.log(Level.INFO, "IncrementalAuth Request is: {}", request);
        return processPayload(request, headers);
    }

    /**
     * This method is responsible for handling request
     * for Capture Operation.
     *
     * @param cpPaymentCaptureRequest: needs a valid request to process
     * @return response for Opera
     */
    @PostMapping("/capture")
    public ResponseEntity<GenericResponse> capture(@RequestHeader HttpHeaders headers, @Valid @RequestBody CPPaymentCaptureRequest cpPaymentCaptureRequest) throws JsonProcessingException, ParseException {
        logger.log(Level.INFO, "Capture Request is: {}", cpPaymentCaptureRequest);
        return processPayload(cpPaymentCaptureRequest, headers);
    }

    /**
     * This method is responsible for handling request
     * for cardVoid operation.
     * @param cpPaymentCardVoidRequest: needs a valid request to process
     * @return response for Opera
     */
    @PostMapping("/void")
    public ResponseEntity<GenericResponse> cardVoid(@RequestHeader HttpHeaders headers, @Valid @RequestBody CPPaymentCardVoidRequest cpPaymentCardVoidRequest) throws JsonProcessingException {
        logger.log(Level.INFO, "CardVoid Request is: {}", cpPaymentCardVoidRequest);
        return processPayload(cpPaymentCardVoidRequest, headers);
    }

    /**
     * This method is responsible for handling request
     * for Refund operation.
     * @param cpPaymentRefundRequest: needs a valid request to process
     * @return response for Opera
     */
    @PostMapping("/refund")
    public ResponseEntity<GenericResponse> refund(@RequestHeader HttpHeaders headers, @Valid @RequestBody CPPaymentRefundRequest cpPaymentRefundRequest) throws JsonProcessingException {
        logger.log(Level.INFO, "Refund Request is: {}", cpPaymentRefundRequest);
        return processPayload(cpPaymentRefundRequest, headers);
    }

    /**
     * This method takes the valid request for Authorization
     * and pass it to cpPaymentProcessingService to process.
     */
    private ResponseEntity<GenericResponse> processPayload(CPPaymentAuthorizationRequest cpPaymentAuthorizationRequest, HttpHeaders headers) throws JsonProcessingException, ParseException {
        sendAuditData(INITIAL_AUTH, INITIAL_AUTH, cpPaymentAuthorizationRequest, INITIAL_AUTH, headers.toSingleValueMap(), null);
        ResponseEntity<GenericResponse> responseEntity = cpPaymentProcessingService.processAuthorizeRequest(cpPaymentAuthorizationRequest, headers);
        sendAuditData(INITIAL_AUTH, INITIAL_AUTH, cpPaymentAuthorizationRequest, INITIAL_AUTH, headers.toSingleValueMap(), responseEntity.getBody());
        return responseEntity;
    }

    /**
     * This method takes the valid request for Incremental Authorization
     * and pass it to cpPaymentProcessingService to process.
     */
    private ResponseEntity<GenericResponse> processPayload(CPPaymentIncrementalAuthRequest request, HttpHeaders headers) throws JsonProcessingException, ParseException {
        sendAuditData(INCREMENTAL_AUTH, INCREMENTAL_AUTH, request, INCREMENTAL_AUTH, headers.toSingleValueMap(), null);
        ResponseEntity<GenericResponse> responseEntity = cpPaymentProcessingService.processIncrementalAuthorizationRequest(request, headers);
        sendAuditData(INCREMENTAL_AUTH, INCREMENTAL_AUTH, request, INCREMENTAL_AUTH, headers.toSingleValueMap(), responseEntity.getBody());
        return responseEntity;
    }

    /**
     * This method takes the valid request for Capture
     * and pass it to cpPaymentProcessingService to process.
     */
    private ResponseEntity<GenericResponse> processPayload(CPPaymentCaptureRequest cpPaymentCaptureRequest, HttpHeaders headers) throws JsonProcessingException, ParseException {
        sendAuditData(CAPTURE, CAPTURE, cpPaymentCaptureRequest, CAPTURE, headers.toSingleValueMap(), null);
        ResponseEntity<GenericResponse> responseEntity = cpPaymentProcessingService.processCaptureRequest(cpPaymentCaptureRequest, headers);
        sendAuditData(CAPTURE, CAPTURE, cpPaymentCaptureRequest, CAPTURE, headers.toSingleValueMap(), responseEntity.getBody());
        return responseEntity;
    }

    /**
     * This method takes the valid request for CardVoid
     * and pass it to cpPaymentProcessingService to process.
     */
    private ResponseEntity<GenericResponse> processPayload(CPPaymentCardVoidRequest cpPaymentCardVoidRequest, HttpHeaders headers) throws JsonProcessingException {
        sendAuditData(VOID, VOID, cpPaymentCardVoidRequest, VOID, headers.toSingleValueMap(), null);
        ResponseEntity<GenericResponse> responseEntity = cpPaymentProcessingService.processCardVoidRequest(cpPaymentCardVoidRequest, headers);
        sendAuditData(VOID, VOID, cpPaymentCardVoidRequest, VOID, headers.toSingleValueMap(), responseEntity.getBody());
        return responseEntity;
    }

    /**
     * This method takes the valid request for Refund
     * and pass it to cpPaymentProcessingService to process.
     */
    private ResponseEntity<GenericResponse> processPayload(CPPaymentRefundRequest cpPaymentRefundRequest, HttpHeaders headers) throws JsonProcessingException {
        sendAuditData(REFUND, REFUND, cpPaymentRefundRequest, REFUND, headers.toSingleValueMap(), null);
        ResponseEntity<GenericResponse> responseEntity = cpPaymentProcessingService.processRefundRequest(cpPaymentRefundRequest, headers);
        sendAuditData(REFUND, REFUND, cpPaymentRefundRequest, REFUND, headers.toSingleValueMap(), responseEntity.getBody());
        return responseEntity;
    }

    private void sendAuditData(String eventName, String eventDescription, Object requestPayload, String method, Map<String, String> requestHeader, Object responsePayload){
        auditEventProducer.sendAuditData(eventName,eventDescription,requestPayload,"shift4",requestHeader,null,"CP-PaymentProcessingService - "+method,responsePayload);
    }
}
