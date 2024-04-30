package com.mgm.pd.cp.resortpayment.service.payment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mgm.pd.cp.payment.common.dto.GenericResponse;
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
import com.mgm.pd.cp.resortpayment.service.router.RouterHelper;
import com.mgm.pd.cp.resortpayment.util.common.PaymentProcessingServiceHelper;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.Optional;

/**
 * This service class is responsible for implementing the methods
 * declared in CpPaymentProcessingService
 * and to provide complete implementation for those methods.
 */
@Service
@AllArgsConstructor
public class CPPaymentProcessingServiceImpl implements CPPaymentProcessingService {
    private SavePaymentService savePaymentService;
    private RouterHelper routerHelper;
    private PaymentProcessingServiceHelper serviceHelper;

    /**
     * This method is responsible to send the Authorization request
     * to Intelligent Router(IR),
     * saving Details to Payment DB, returning response or Error back to upstream systems.
     *
     * @param request: sent to IR
     * @param headers: Request Headers
     * @return response from IR
     */
    @Override
    public ResponseEntity<GenericResponse> processAuthorizeRequest(CPPaymentAuthorizationRequest request, HttpHeaders headers) throws JsonProcessingException, ParseException {
        //Used for business Validations of Authorize Request
        serviceHelper.validateAuthorizeRequest(request);
        //adding headers in request before sending to IR
        request = serviceHelper.mapHeadersInRequest(request, headers);
        AuthorizationRouterResponse authRouterResponse = null;
        Payment payment;
        try{
            //sending request to IR
            authRouterResponse = routerHelper.sendAuthorizeRequestToRouter(request, headers);
        }
        finally {
            //saving combined response of (Request and response from IR) to Payment DB
            payment = savePaymentService.saveAuthorizationPayment(request, authRouterResponse);
        }
        //converting and returning response for upstream system
        return serviceHelper.response(payment, request);
    }

    /**
     * This method is responsible to send the Incremental Authorization request
     * to Intelligent Router(IR),
     * saving Details to Payment DB, returning response or Error back to upstream systems.
     *
     * @param request: sent to IR
     * @param headers: Request Headers
     * @return response from IR
     */
    @Override
    public ResponseEntity<GenericResponse> processIncrementalAuthorizationRequest(CPPaymentIncrementalAuthRequest request, HttpHeaders headers) throws JsonProcessingException, ParseException {
        //can be used for businessValidations and finding initial Payment as pre-requisite for processing of Incremental Authorization Request
        Optional<Payment> optionalInitialPayment = serviceHelper.validateIncrementalAuthorizationRequestAndReturnInitialPayment(request, headers);
        //adding headers in request before sending to IR
        request = serviceHelper.mapHeadersInRequest(request, headers);
        IncrementalAuthorizationRouterResponse irResponse = null;
        Payment payment;
        try{
            //sending request to IR
            irResponse = routerHelper.sendIncrementalAuthorizationRequestToRouter(request, optionalInitialPayment.orElse(null), headers);
        }
        finally {
            //saving combined response of (Request and response from IR) to Payment DB
            payment = savePaymentService.saveIncrementalAuthorizationPayment(request, irResponse, optionalInitialPayment.orElse(null));
        }
        //converting and returning response for upstream system
        return serviceHelper.response(payment, request);
    }

    /**
     * This method is responsible to send the Capture request
     * to Intelligent Router(IR),
     * saving Details to Payment DB, returning response or Error back to upstream systems.
     *
     * @param request: sent to IR
     * @param headers: Request Headers
     * @return response from IR
     */
    @Override
    public ResponseEntity<GenericResponse> processCaptureRequest(CPPaymentCaptureRequest request, HttpHeaders headers) throws JsonProcessingException, ParseException {
        //used for businessValidations and finding initial Payment as pre-requisite for processing of Capture Request
        Optional<Payment> optionalInitialPayment = serviceHelper.validateCaptureRequestAndReturnInitialPayment(request, headers);
        //adding headers in request before sending to IR
        request = serviceHelper.mapHeadersInRequest(request, headers);
        CaptureRouterResponse crResponse = null;
        Payment payment;
        try{
            //sending request to IR
            crResponse = routerHelper.sendCaptureRequestToRouter(request, optionalInitialPayment.orElse(null), headers);
        }
        finally {
            //saving combined response of (Request and response from IR) to Payment DB
            payment = savePaymentService.saveCaptureAuthPayment(request, crResponse, optionalInitialPayment.orElse(null));
        }
        //converting and returning response for upstream system
        return serviceHelper.response(payment, request);
    }

    /**
     * This method is responsible to send the Card Void request
     * to Intelligent Router(IR),
     * saving Details to Payment DB, returning response or Error back to upstream systems.
     *
     * @param request: sent to IR
     * @param headers: Request Headers
     * @return response from IR
     */
    @Override
    public ResponseEntity<GenericResponse> processCardVoidRequest(CPPaymentCardVoidRequest request, HttpHeaders headers) throws JsonProcessingException {
        //used for businessValidations and finding initial Payment as pre-requisite for processing of Card Void Request
        Optional<Payment> optionalInitialPayment = serviceHelper.validateCardVoidRequestAndReturnInitialPayment(request, headers);
        //adding headers in request before sending to IR
        request = serviceHelper.mapHeadersInRequest(request, headers);
        CardVoidRouterResponse cvrResponse = null;
        Payment payment;
        try{
            //sending request to IR
            cvrResponse = routerHelper.sendCardVoidRequestToRouter(request, optionalInitialPayment.orElse(null), headers);
        }
        finally{
            //saving combined response of (Request and response from IR) to Payment DB
            payment = savePaymentService.saveCardVoidAuthPayment(request, cvrResponse, optionalInitialPayment.orElse(null));
        }
        //converting and returning response for upstream system
        return serviceHelper.response(payment, request);
    }

    /**
     * This method is responsible to send the Refund request
     * to Intelligent Router(IR),
     * saving Details to Payment DB, returning response or Error back to upstream systems.
     *
     * @param request: sent to IR
     * @param headers: Request Headers
     * @return response from IR
     */
    @Override
    public ResponseEntity<GenericResponse> processRefundRequest(CPPaymentRefundRequest request, HttpHeaders headers) throws JsonProcessingException {
        //used for businessValidations of Refund Request
        serviceHelper.validateRefundRequest(request);
        //adding headers in request before sending to IR
        request = serviceHelper.mapHeadersInRequest(request, headers);
        RefundRouterResponse rrResponse = null;
        Payment payment;
        try{
            //sending request to IR
            rrResponse = routerHelper.sendRefundRequestToRouter(request, headers);
        }
        finally {
            //saving combined response of (Request and response from IR) to Payment DB
            payment = savePaymentService.saveRefundPayment(request, rrResponse);
        }
        //converting and returning response for upstream system
        return serviceHelper.response(payment, request);
    }
}
