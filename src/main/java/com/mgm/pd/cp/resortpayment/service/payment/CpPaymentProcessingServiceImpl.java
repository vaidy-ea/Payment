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
import com.mgm.pd.cp.resortpayment.service.router.RouterService;
import com.mgm.pd.cp.resortpayment.util.common.PaymentProcessingServiceHelper;
import feign.FeignException;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.mgm.pd.cp.payment.common.constant.ApplicationConstants.INITIAL_PAYMENT_IS_MISSING;

@Service
@AllArgsConstructor
public class CpPaymentProcessingServiceImpl implements CpPaymentProcessingService {
    private FindPaymentService findPaymentService;
    private SavePaymentService savePaymentService;
    private RouterService routerService;
    private PaymentProcessingServiceHelper serviceHelper;

    /**
     *
     * @param request
     * @return response
     * @throws JsonProcessingException
     */
    @Override
    public ResponseEntity<GenericResponse> processIncrementalAuthorizationRequest(CPPaymentIncrementalAuthRequest request) throws JsonProcessingException {
        Optional<Payment> optionalInitialPayment = serviceHelper.getInitialAuthPayment(request);
        if (optionalInitialPayment.isPresent()) {
            IncrementalAuthorizationRouterResponse irResponse = null;
            Payment payment;
            try{
                irResponse = routerService.sendIncrementalAuthorizationRequestToRouter(request, optionalInitialPayment.get().getIncrementalAuthInvoiceId());
            } catch(FeignException feignEx) {
                irResponse = IncrementalAuthorizationRouterResponse.builder().comments(serviceHelper.getCommentsFromException(feignEx)).build();
                throw feignEx;
            }
            finally {
                payment = savePaymentService.saveIncrementalAuthorizationPayment(request, irResponse);
            }
            return serviceHelper.responseForOpera(payment);
        }
        request.setComments(INITIAL_PAYMENT_IS_MISSING);
        savePaymentService.saveIncrementalAuthorizationPayment(request, null);
        return serviceHelper.initialPaymentIsMissing();
    }

    @Override
    public ResponseEntity<GenericResponse> processAuthorizeRequest(CPPaymentAuthorizationRequest cpPaymentAuthorizationRequest) throws JsonProcessingException {
        AuthorizationRouterResponse authRouterResponse = null;
        Payment payment;
        try{
            authRouterResponse = routerService.sendAuthorizeRequestToRouter(cpPaymentAuthorizationRequest, 12345L);
        } catch(FeignException feignEx) {
            authRouterResponse = AuthorizationRouterResponse.builder().comments(serviceHelper.getCommentsFromException(feignEx)).build();
            throw feignEx;
        }
        finally {
            payment = savePaymentService.saveAuthorizationPayment(cpPaymentAuthorizationRequest, authRouterResponse);
        }
        return serviceHelper.responseForOpera(payment);
    }

    /**
     *
     * @param request
     * @return response
     * @throws JsonProcessingException
     */
    @Override
    public ResponseEntity<GenericResponse> processCaptureRequest(CPPaymentCaptureRequest request) throws JsonProcessingException {
        Optional<Payment> optionalInitialPayment = serviceHelper.getInitialAuthPayment(request);
        if (optionalInitialPayment.isPresent()) {
            Payment initialPayment = optionalInitialPayment.get();
            Payment payment;
            CaptureRouterResponse crResponse = null;
            try{
                crResponse = routerService.sendCaptureRequestToRouter(request, initialPayment.getIncrementalAuthInvoiceId());
            } catch(FeignException feignEx) {
                crResponse = CaptureRouterResponse.builder().comments(serviceHelper.getCommentsFromException(feignEx)).build();
                throw feignEx;
            } finally {
                payment = savePaymentService.saveCaptureAuthPayment(request, crResponse, initialPayment.getAuthTotalAmount());
            }
            return serviceHelper.responseForOpera(payment);
        }
        request.setComments(INITIAL_PAYMENT_IS_MISSING);
        savePaymentService.saveCaptureAuthPayment(request, null, null);
        return serviceHelper.initialPaymentIsMissing();
    }

    /**
     *
     * @param cvRequest
     * @return response
     * @throws JsonProcessingException
     */
    @Override
    public ResponseEntity<GenericResponse> processCardVoidRequest(CPPaymentCardVoidRequest cvRequest) throws JsonProcessingException {
        Optional<Payment> optionalInitialPayment = findPaymentService.getPaymentDetails(cvRequest.getPropertyCode(), cvRequest.getResvNameID());
        if (optionalInitialPayment.isPresent()) {
            Payment payment;
            CardVoidRouterResponse cvrResponse = null;
            try{
                cvrResponse = routerService.sendCardVoidRequestToRouter(cvRequest, optionalInitialPayment.get().getIncrementalAuthInvoiceId());
            } catch(FeignException feignEx) {
                cvrResponse = serviceHelper.addCommentsForCardVoidResponse(feignEx);
                throw feignEx;
            } finally {
                payment = savePaymentService.saveCardVoidAuthPayment(cvRequest, cvrResponse);
            }
            return serviceHelper.responseForOpera(payment);
        }
        cvRequest.setComments(INITIAL_PAYMENT_IS_MISSING);
        savePaymentService.saveCardVoidAuthPayment(cvRequest, null);
        return serviceHelper.initialPaymentIsMissing();
    }

    @Override
    public ResponseEntity<GenericResponse> processRefundRequest(CPPaymentRefundRequest request) throws JsonProcessingException {
        Optional<Payment> optionalInitialPayment = serviceHelper.getInitialAuthPayment(request);
        if (optionalInitialPayment.isPresent()) {
            RefundRouterResponse rrResponse = null;
            Payment payment;
            try{
                rrResponse = routerService.sendRefundRequestToRouter(request, optionalInitialPayment.get().getIncrementalAuthInvoiceId());
            } catch(FeignException feignEx) {
                rrResponse = RefundRouterResponse.builder().comments(serviceHelper.getCommentsFromException(feignEx)).build();
                throw feignEx;
            }
            finally {
                payment = savePaymentService.saveRefundPayment(request, rrResponse);
            }
            return serviceHelper.responseForOpera(payment);
        }
        request.setComments(INITIAL_PAYMENT_IS_MISSING);
        savePaymentService.saveRefundPayment(request, null);
        return serviceHelper.initialPaymentIsMissing();
    }
}
