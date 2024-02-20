package com.mgm.pd.cp.resortpayment.service.payment;

import com.mgm.pd.cp.resortpayment.dto.authorize.AuthorizationRouterResponse;
import com.mgm.pd.cp.resortpayment.dto.authorize.CPPaymentAuthorizationRequest;
import com.mgm.pd.cp.resortpayment.dto.capture.CPPaymentCaptureRequest;
import com.mgm.pd.cp.resortpayment.dto.capture.CaptureRouterResponse;
import com.mgm.pd.cp.resortpayment.dto.cardvoid.CPPaymentCardVoidRequest;
import com.mgm.pd.cp.resortpayment.dto.cardvoid.CardVoidRouterResponse;
import com.mgm.pd.cp.resortpayment.dto.incrementalauth.CPPaymentIncrementalRequest;
import com.mgm.pd.cp.resortpayment.dto.incrementalauth.IncrementalAuthorizationRouterResponse;
import com.mgm.pd.cp.resortpayment.model.Payment;
import org.springframework.transaction.annotation.Transactional;

public interface SavePaymentService {
    @Transactional
    Payment saveIncrementalAuthorizationPayment(CPPaymentIncrementalRequest incrementalRequest, IncrementalAuthorizationRouterResponse irResponse);
    @Transactional
    public Payment saveAuthPayment(CPPaymentAuthorizationRequest authRequest, AuthorizationRouterResponse irResponse);

    @Transactional
    Payment saveCaptureAuthPayment(CPPaymentCaptureRequest captureRequest, CaptureRouterResponse crResponse, Double authAmountRequested);
    @Transactional
    Payment saveCardVoidAuthPayment(CPPaymentCardVoidRequest voidRequest, CardVoidRouterResponse vrResponse);
}
