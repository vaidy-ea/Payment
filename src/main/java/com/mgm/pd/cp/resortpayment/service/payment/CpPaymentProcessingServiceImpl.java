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
import feign.FeignException;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.mgm.pd.cp.payment.common.constant.ApplicationConstants.INITIAL_PAYMENT_IS_MISSING;

/**
 * This service class is responsible for implementing the methods
 * declared in CpPaymentProcessingService
 * and to provide complete implementation for those methods.
 */
@Service
@AllArgsConstructor
public class CpPaymentProcessingServiceImpl implements CpPaymentProcessingService {
    private SavePaymentService savePaymentService;
    private RouterHelper routerHelper;
    private PaymentProcessingServiceHelper serviceHelper;

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
    public ResponseEntity<GenericResponse<?>> processIncrementalAuthorizationRequest(CPPaymentIncrementalAuthRequest request, HttpHeaders headers) throws JsonProcessingException {
        request = serviceHelper.mapHeadersInRequest(request, headers);
        //finding initial Payment as pre-requisite for processing of Incremental Authorization
        Optional<Payment> optionalInitialPayment = serviceHelper.getInitialAuthPayment(request);
        if (optionalInitialPayment.isPresent()) {
            IncrementalAuthorizationRouterResponse irResponse = null;
            Payment payment;
            try{
                //sending request to IR
                irResponse = routerHelper.sendIncrementalAuthorizationRequestToRouter(request, optionalInitialPayment.get(), headers);
            } catch(FeignException feignEx) {
                //adding comments in case of Exception from IR
                irResponse = IncrementalAuthorizationRouterResponse.builder().comments(serviceHelper.getCommentsFromException(feignEx)).build();
                //throwing back to Exception Handler(CPPaymentProcessingExceptionHandler.java)
                throw feignEx;
            }
            finally {
                //saving combined response of (Request and response from IR) to Payment DB
                payment = savePaymentService.saveIncrementalAuthorizationPayment(request, irResponse);
            }
            //converting and returning response for upstream system
            return serviceHelper.response(payment);
        }
        //Saving the comments in Payment DB and sending an Error Response to upstreams system
        request.setComments(INITIAL_PAYMENT_IS_MISSING);
        request.setIncrementalAuthInvoiceId(null);
        savePaymentService.saveIncrementalAuthorizationPayment(request, null);
        return serviceHelper.initialPaymentIsMissing();
    }

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
    public ResponseEntity<GenericResponse<?>> processAuthorizeRequest(CPPaymentAuthorizationRequest request, HttpHeaders headers) throws JsonProcessingException {
        request = serviceHelper.mapHeadersInRequest(request, headers);
        AuthorizationRouterResponse authRouterResponse = null;
        Payment payment;
        try{
            //sending request to IR
            authRouterResponse = routerHelper.sendAuthorizeRequestToRouter(request, 12345L, headers);
        } catch(FeignException feignEx) {
            //adding comments in case of Exception from IR
            authRouterResponse = AuthorizationRouterResponse.builder().comments(serviceHelper.getCommentsFromException(feignEx)).build();
            //throwing back to Exception Handler(CPPaymentProcessingExceptionHandler.java)
            throw feignEx;
        }
        finally {
            //saving combined response of (Request and response from IR) to Payment DB
            payment = savePaymentService.saveAuthorizationPayment(request, authRouterResponse);
        }
        //converting and returning response for upstream system
        return serviceHelper.response(payment);
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
    public ResponseEntity<GenericResponse<?>> processCaptureRequest(CPPaymentCaptureRequest request, HttpHeaders headers) throws JsonProcessingException {
        request = serviceHelper.mapHeadersInRequest(request, headers);
        //finding initial Payment as pre-requisite for processing of Capture
        Optional<Payment> optionalInitialPayment = serviceHelper.getInitialAuthPayment(request);
        if (optionalInitialPayment.isPresent()) {
            Payment initialPayment = optionalInitialPayment.get();
            Payment payment;
            CaptureRouterResponse crResponse = null;
            try{
                //sending request to IR
                crResponse = routerHelper.sendCaptureRequestToRouter(request, initialPayment, headers);
            } catch(FeignException feignEx) {
                //adding comments in case of Exception from IR
                crResponse = CaptureRouterResponse.builder().comments(serviceHelper.getCommentsFromException(feignEx)).build();
                //throwing back to Exception Handler(CPPaymentProcessingExceptionHandler.java)
                throw feignEx;
            } finally {
                //saving combined response of (Request and response from IR) to Payment DB
                payment = savePaymentService.saveCaptureAuthPayment(request, crResponse, initialPayment);
            }
            //converting and returning response for upstream system
            return serviceHelper.response(payment);
        }
        //Saving the comments in Payment DB and sending an Error Response to upstreams system
        request.setComments(INITIAL_PAYMENT_IS_MISSING);
        request.setIncrementalAuthInvoiceId(null);
        savePaymentService.saveCaptureAuthPayment(request, null, null);
        return serviceHelper.initialPaymentIsMissing();
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
    public ResponseEntity<GenericResponse<?>> processCardVoidRequest(CPPaymentCardVoidRequest request, HttpHeaders headers) throws JsonProcessingException {
        request = serviceHelper.mapHeadersInRequest(request, headers);
        //finding initial Payment as pre-requisite for processing of Card Void Request
        Optional<Payment> optionalInitialPayment = serviceHelper.getInitialAuthPayment(request);
        if (optionalInitialPayment.isPresent()) {
            Payment payment;
            CardVoidRouterResponse cvrResponse = null;
            try{
                //sending request to IR
                cvrResponse = routerHelper.sendCardVoidRequestToRouter(request, optionalInitialPayment.get(), headers);
            } catch(FeignException feignEx) {
                //adding comments in case of Exception from IR
                cvrResponse = CardVoidRouterResponse.builder().comments(serviceHelper.getCommentsFromException(feignEx)).build();
                //throwing back to Exception Handler(CPPaymentProcessingExceptionHandler.java)
                throw feignEx;
            } finally {
                //saving combined response of (Request and response from IR) to Payment DB
                payment = savePaymentService.saveCardVoidAuthPayment(request, cvrResponse);
            }
            //converting and returning response for upstream system
            return serviceHelper.response(payment);
        }
        //Saving the comments in Payment DB and sending an Error Response to upstreams system
        request.setComments(INITIAL_PAYMENT_IS_MISSING);
        request.setIncrementalAuthInvoiceId(null);
        savePaymentService.saveCardVoidAuthPayment(request, null);
        return serviceHelper.initialPaymentIsMissing();
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
    public ResponseEntity<GenericResponse<?>> processRefundRequest(CPPaymentRefundRequest request, HttpHeaders headers) throws JsonProcessingException {
        request = serviceHelper.mapHeadersInRequest(request, headers);
        //finding initial Payment as pre-requisite for processing of Refund Request
        Optional<Payment> optionalInitialPayment = serviceHelper.getInitialAuthPayment(request);
        if (optionalInitialPayment.isPresent()) {
            RefundRouterResponse rrResponse = null;
            Payment payment;
            try{
                //sending request to IR
                rrResponse = routerHelper.sendRefundRequestToRouter(request, optionalInitialPayment.get(), headers);
            } catch(FeignException feignEx) {
                //adding comments in case of Exception from IR
                rrResponse = RefundRouterResponse.builder().comments(serviceHelper.getCommentsFromException(feignEx)).build();
                //throwing back to Exception Handler(CPPaymentProcessingExceptionHandler.java)
                throw feignEx;
            }
            finally {
                //saving combined response of (Request and response from IR) to Payment DB
                payment = savePaymentService.saveRefundPayment(request, rrResponse);
            }
            //converting and returning response for upstream system
            return serviceHelper.response(payment);
        }
        //Saving the comments in Payment DB and sending an Error Response to upstreams system
        request.setComments(INITIAL_PAYMENT_IS_MISSING);
        request.setIncrementalAuthInvoiceId(null);
        savePaymentService.saveRefundPayment(request, null);
        return serviceHelper.initialPaymentIsMissing();
    }
}
