package com.mgm.pd.cp.resortpayment.service.payment;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.mgm.pd.cp.payment.common.dto.GenericResponse;
import com.mgm.pd.cp.resortpayment.dto.authorize.CPPaymentAuthorizationRequest;
import com.mgm.pd.cp.resortpayment.dto.capture.CPPaymentCaptureRequest;
import com.mgm.pd.cp.resortpayment.dto.cardvoid.CPPaymentCardVoidRequest;
import com.mgm.pd.cp.resortpayment.dto.incrementalauth.CPPaymentIncrementalAuthRequest;
import com.mgm.pd.cp.resortpayment.dto.refund.CPPaymentRefundRequest;
import org.springframework.http.ResponseEntity;

import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;

public interface CpPaymentProcessingService {
    ResponseEntity<GenericResponse> processIncrementalAuthorizationRequest(CPPaymentIncrementalAuthRequest request) throws JsonProcessingException, IntrospectionException, InvocationTargetException, IllegalAccessException;
    ResponseEntity<GenericResponse> processAuthorizeRequest(CPPaymentAuthorizationRequest cpPaymentIncrementalRequest) throws JsonProcessingException;
    ResponseEntity<GenericResponse> processCaptureRequest(CPPaymentCaptureRequest captureRequest) throws JsonProcessingException;
    ResponseEntity<GenericResponse> processCardVoidRequest(CPPaymentCardVoidRequest voidRequest) throws JsonProcessingException;
    ResponseEntity<GenericResponse> processRefundRequest(CPPaymentRefundRequest cpPaymentRefundRequest) throws JsonProcessingException;
}
