package com.mgm.pd.cp.resortpayment.service.payment;

import com.mgm.pd.cp.payment.common.model.Payment;
import com.mgm.pd.cp.resortpayment.dto.authorize.AuthorizationRouterResponse;
import com.mgm.pd.cp.resortpayment.dto.authorize.CPPaymentAuthorizationRequest;
import com.mgm.pd.cp.resortpayment.dto.capture.CPPaymentCaptureRequest;
import com.mgm.pd.cp.resortpayment.dto.capture.CaptureRouterResponse;
import com.mgm.pd.cp.resortpayment.dto.cardvoid.CPPaymentCardVoidRequest;
import com.mgm.pd.cp.resortpayment.dto.cardvoid.CardVoidRouterResponse;
import com.mgm.pd.cp.resortpayment.dto.incrementalauth.CPPaymentIncrementalAuthRequest;
import com.mgm.pd.cp.resortpayment.dto.incrementalauth.IncrementalAuthorizationRouterResponse;
import com.mgm.pd.cp.resortpayment.dto.refund.CPPaymentRefundRequest;
import com.mgm.pd.cp.resortpayment.dto.refund.RefundRouterResponse;
import org.springframework.transaction.annotation.Transactional;

import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;

public interface SavePaymentService {
    @Transactional
    Payment saveIncrementalAuthorizationPayment(CPPaymentIncrementalAuthRequest incrementalRequest, IncrementalAuthorizationRouterResponse irResponse) throws IntrospectionException, InvocationTargetException, IllegalAccessException;
    @Transactional
    Payment saveAuthorizationPayment(CPPaymentAuthorizationRequest authRequest, AuthorizationRouterResponse irResponse);
    @Transactional
    Payment saveCaptureAuthPayment(CPPaymentCaptureRequest captureRequest, CaptureRouterResponse crResponse, Double authAmountRequested);
    @Transactional
    Payment saveCardVoidAuthPayment(CPPaymentCardVoidRequest voidRequest, CardVoidRouterResponse vrResponse);
    @Transactional
    Payment saveRefundPayment(CPPaymentRefundRequest request, RefundRouterResponse rrResponse);
}
