package com.mgm.pd.cp.resortpayment.service;

import com.mgm.pd.cp.resortpayment.dto.capture.CPPaymentCaptureRequest;
import com.mgm.pd.cp.resortpayment.dto.capture.CaptureRouterResponse;
import com.mgm.pd.cp.resortpayment.dto.cardvoid.CPPaymentVoidRequest;
import com.mgm.pd.cp.resortpayment.dto.cardvoid.VoidRouterResponse;
import com.mgm.pd.cp.resortpayment.dto.incrementalauth.CPPaymentIncrementalRequest;
import com.mgm.pd.cp.resortpayment.dto.incrementalauth.IncrementalRouterResponse;
import com.mgm.pd.cp.resortpayment.model.Payment;

public interface SavePaymentService {
    Payment saveIncrementalAuthPayment(CPPaymentIncrementalRequest incrementalRequest, IncrementalRouterResponse irResponse);
    Payment saveCaptureAuthPayment(CPPaymentCaptureRequest captureRequest, CaptureRouterResponse crResponse, Double authAmountRequested);
    Payment saveVoidAuthPayment(CPPaymentVoidRequest voidRequest, VoidRouterResponse vrResponse);
}
