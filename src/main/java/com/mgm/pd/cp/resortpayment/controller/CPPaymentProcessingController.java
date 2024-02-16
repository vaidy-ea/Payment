package com.mgm.pd.cp.resortpayment.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mgm.pd.cp.resortpayment.dto.capture.CPPaymentCaptureRequest;
import com.mgm.pd.cp.resortpayment.dto.cardvoid.CPPaymentVoidRequest;
import com.mgm.pd.cp.resortpayment.dto.common.GenericResponse;
import com.mgm.pd.cp.resortpayment.dto.incrementalauth.CPPaymentIncrementalRequest;
import com.mgm.pd.cp.resortpayment.service.CpPaymentProcessingService;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/services/v1/payments")
public class CPPaymentProcessingController {
    private static final Logger logger = LogManager.getLogger(CPPaymentProcessingController.class);

    @Autowired
    CpPaymentProcessingService cpPaymentProcessingService;

    @PostMapping("/incrementalauth")
    public ResponseEntity<GenericResponse> incrementalAuth(@Valid @RequestBody CPPaymentIncrementalRequest cpPaymentIncrementalRequest) throws JsonProcessingException {
        logger.log(Level.DEBUG, "CPPaymentIncrementalRequest Request in DEBUG is : " + cpPaymentIncrementalRequest.getWorkstation());
        return processPayload(cpPaymentIncrementalRequest);
    }

    @PostMapping("/capture")
    public ResponseEntity<GenericResponse> capture(@Valid @RequestBody CPPaymentCaptureRequest cpPaymentCaptureRequest) throws JsonProcessingException {
        logger.log(Level.DEBUG, "CPPaymentCaptureRequest Request in DEBUG is : " + cpPaymentCaptureRequest.getWorkstation());
        return processPayload(cpPaymentCaptureRequest);
    }

    @PostMapping("/void")
    public ResponseEntity<GenericResponse> completeVoid(@Valid @RequestBody CPPaymentVoidRequest cpPaymentVoidRequest) throws JsonProcessingException {
        logger.log(Level.DEBUG, "CPPaymentVoidRequest Request in DEBUG is : " + cpPaymentVoidRequest.getWorkstation());
        return processPayload(cpPaymentVoidRequest);
    }

    private ResponseEntity<GenericResponse> processPayload(CPPaymentIncrementalRequest incrementalRequest) throws JsonProcessingException {
        return cpPaymentProcessingService.processIncrementalRequest(incrementalRequest);
    }

    private ResponseEntity<GenericResponse> processPayload(CPPaymentCaptureRequest cpPaymentCaptureRequest) throws JsonProcessingException {
        return cpPaymentProcessingService.processCaptureRequest(cpPaymentCaptureRequest);
    }

    private ResponseEntity<GenericResponse> processPayload(CPPaymentVoidRequest cpPaymentVoidRequest) throws JsonProcessingException {
        return cpPaymentProcessingService.processVoidRequest(cpPaymentVoidRequest);
    }
}
