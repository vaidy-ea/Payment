package com.mgm.pd.cp.resortpayment.service.payment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mgm.pd.cp.payment.common.dto.ErrorResponse;
import com.mgm.pd.cp.payment.common.dto.GenericResponse;
import com.mgm.pd.cp.payment.common.dto.opera.OperaResponse;
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
import com.mgm.pd.cp.resortpayment.model.Payment;
import com.mgm.pd.cp.resortpayment.service.router.RouterService;
import com.mgm.pd.cp.resortpayment.util.common.Converter;
import com.mgm.pd.cp.resortpayment.util.common.PaymentProcessingServiceHelper;
import feign.FeignException;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;

import static com.mgm.pd.cp.payment.common.constant.ApplicationConstants.INITIAL_PAYMENT_IS_MISSING;

@Service
@AllArgsConstructor
public class CpPaymentProcessingServiceImpl implements CpPaymentProcessingService {
    Converter converter;
    FindPaymentService findPaymentService;
    SavePaymentService savePaymentService;
    RouterService routerService;
    PaymentProcessingServiceHelper serviceHelper;

    /**
     *
     * @param request
     * @return response
     * @throws JsonProcessingException
     */
    @Override
    public ResponseEntity<GenericResponse> processIncrementalAuthorizationRequest(CPPaymentIncrementalAuthRequest request) throws JsonProcessingException, IntrospectionException, InvocationTargetException, IllegalAccessException {
        Optional<Payment> optionalInitialPayment = findPaymentService.getPaymentDetails(serviceHelper.getValueByName(request, "propertyIdentifier"),
                request.getTransactionDetails().getSaleItem().getSaleReferenceIdentifier());
        if (optionalInitialPayment.isPresent()) {
            OperaResponse operaResponse;
            IncrementalAuthorizationRouterResponse irResponse = null;
            Payment payment;
            try{
                irResponse = routerService.sendIncrementalAuthorizationRequestToRouter(request, optionalInitialPayment.get().getIncrementalAuthInvoiceId());
            } catch(FeignException feignEx) {
                irResponse = serviceHelper.addCommentsForIncrementalAuthResponse(feignEx);
                throw feignEx;
            }
            finally {
                payment = savePaymentService.saveIncrementalAuthorizationPayment(request, irResponse);
            }
            operaResponse = converter.convert(payment);
            return response(operaResponse, HttpStatus.CREATED);
        }
        request.setComments(INITIAL_PAYMENT_IS_MISSING);
        savePaymentService.saveIncrementalAuthorizationPayment(request, null);
        return initialPaymentIsMissing();
    }

    @Override
    public ResponseEntity<GenericResponse> processAuthorizeRequest(CPPaymentAuthorizationRequest cpPaymentAuthorizationRequest) throws JsonProcessingException {
        OperaResponse operaResponse;
        AuthorizationRouterResponse authRouterResponse = null;
        Payment payment;
        try{
            authRouterResponse = routerService.sendAuthorizeRequestToRouter(cpPaymentAuthorizationRequest, 12345L);
        } catch(FeignException feignEx) {
            authRouterResponse = serviceHelper.addCommentsForAuthorizationAuthResponse(feignEx);
            throw feignEx;
        }
        finally {
            payment = savePaymentService.saveAuthorizationPayment(cpPaymentAuthorizationRequest, authRouterResponse);
        }
        operaResponse = converter.convert(payment);
        return response(operaResponse, HttpStatus.CREATED);
    }

    /**
     *
     * @param request
     * @return response
     * @throws JsonProcessingException
     */
    @Override
    public ResponseEntity<GenericResponse> processCaptureRequest(CPPaymentCaptureRequest request) throws JsonProcessingException {
        Optional<Payment> optionalInitialPayment = findPaymentService.getPaymentDetails(serviceHelper.getValueByName(request, "propertyIdentifier"),
                request.getTransactionDetails().getSaleItem().getSaleReferenceIdentifier());
        if (optionalInitialPayment.isPresent()) {
            Payment initialPayment = optionalInitialPayment.get();
            OperaResponse operaCaptureResponse;
            Payment payment;
            CaptureRouterResponse crResponse = null;
            try{
                crResponse = routerService.sendCaptureRequestToRouter(request, initialPayment.getIncrementalAuthInvoiceId());
            } catch(FeignException feignEx) {
                crResponse = serviceHelper.addCommentsForCaptureResponse(feignEx);
                throw feignEx;
            } finally {
                payment = savePaymentService.saveCaptureAuthPayment(request, crResponse, initialPayment.getAuthTotalAmount());
            }
            operaCaptureResponse = converter.convert(payment);
            return response(operaCaptureResponse, HttpStatus.CREATED);
        }
        request.setComments(INITIAL_PAYMENT_IS_MISSING);
        savePaymentService.saveCaptureAuthPayment(request, null, null);
        return initialPaymentIsMissing();
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
            OperaResponse operaCardVoidResponse;
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
            operaCardVoidResponse = converter.convert(payment);
            return response(operaCardVoidResponse, HttpStatus.CREATED);
        }
        cvRequest.setComments(INITIAL_PAYMENT_IS_MISSING);
        savePaymentService.saveCardVoidAuthPayment(cvRequest, null);
        return initialPaymentIsMissing();
    }

    @Override
    public ResponseEntity<GenericResponse> processRefundRequest(CPPaymentRefundRequest request) throws JsonProcessingException {
        Optional<Payment> optionalInitialPayment = findPaymentService.getPaymentDetails(serviceHelper.getValueByName(request, "propertyIdentifier"),
                request.getTransactionDetails().getSaleItem().getSaleReferenceIdentifier());
        if (optionalInitialPayment.isPresent()) {
            OperaResponse operaResponse;
            RefundRouterResponse rrResponse = null;
            Payment payment;
            try{
                rrResponse = routerService.sendRefundRequestToRouter(request, optionalInitialPayment.get().getIncrementalAuthInvoiceId());
            } catch(FeignException feignEx) {
                rrResponse = serviceHelper.addCommentsForRefundResponse(feignEx);
                throw feignEx;
            }
            finally {
                payment = savePaymentService.saveRefundPayment(request, rrResponse);
            }
            operaResponse = converter.convert(payment);
            return response(operaResponse, HttpStatus.CREATED);
        }
        request.setComments(INITIAL_PAYMENT_IS_MISSING);
        savePaymentService.saveRefundPayment(request, null);
        return initialPaymentIsMissing();
    }

    private ResponseEntity<GenericResponse> initialPaymentIsMissing() {
        return response(new ErrorResponse(null, 422, INITIAL_PAYMENT_IS_MISSING,
                INITIAL_PAYMENT_IS_MISSING, null, null, null), HttpStatus.UNPROCESSABLE_ENTITY);
    }

    private <D> ResponseEntity<GenericResponse> response(D data, HttpStatus status) {
        return new ResponseEntity<>(GenericResponse.builder().data(data).build(), status);
    }
}