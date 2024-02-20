package com.mgm.pd.cp.resortpayment.service.router;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mgm.pd.cp.resortpayment.dto.authorize.AuthorizationRouterResponse;
import com.mgm.pd.cp.resortpayment.dto.authorize.CPPaymentAuthorizationRequest;
import com.mgm.pd.cp.resortpayment.dto.capture.CPPaymentCaptureRequest;
import com.mgm.pd.cp.resortpayment.dto.capture.CaptureRouterResponse;
import com.mgm.pd.cp.resortpayment.dto.cardvoid.CPPaymentCardVoidRequest;
import com.mgm.pd.cp.resortpayment.dto.cardvoid.CardVoidRouterResponse;
import com.mgm.pd.cp.resortpayment.dto.incrementalauth.CPPaymentIncrementalRequest;
import com.mgm.pd.cp.resortpayment.dto.incrementalauth.IncrementalAuthorizationRouterResponse;

public interface RouterService {
    IncrementalAuthorizationRouterResponse sendIncrementalAuthorizationRequestToRouter(CPPaymentIncrementalRequest incrementalRequest, Long incrementalAuthInvoiceId) throws JsonProcessingException;
    AuthorizationRouterResponse sendAuthorizeRequestToRouter(CPPaymentAuthorizationRequest authorizationRequest, Long incrementalAuthInvoiceId) throws JsonProcessingException ;
    CaptureRouterResponse sendCaptureRequestToRouter(CPPaymentCaptureRequest captureRequest, Long incrementalAuthInvoiceId) throws JsonProcessingException;
    CardVoidRouterResponse sendCardVoidRequestToRouter(CPPaymentCardVoidRequest cvRequest, Long incrementalAuthInvoiceId) throws JsonProcessingException;
}
