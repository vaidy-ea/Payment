package com.mgm.pd.cp.resortpayment.service.payment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mgm.pd.cp.resortpayment.dto.authorize.AuthorizationRouterResponse;
import com.mgm.pd.cp.resortpayment.dto.authorize.CPPaymentAuthorizationRequest;
import com.mgm.pd.cp.resortpayment.dto.capture.CPPaymentCaptureRequest;
import com.mgm.pd.cp.resortpayment.dto.capture.CaptureRouterResponse;
import com.mgm.pd.cp.resortpayment.dto.cardvoid.CPPaymentCardVoidRequest;
import com.mgm.pd.cp.resortpayment.dto.cardvoid.CardVoidRouterResponse;
import com.mgm.pd.cp.resortpayment.dto.common.GenericResponse;
import com.mgm.pd.cp.resortpayment.dto.error.ErrorResponse;
import com.mgm.pd.cp.resortpayment.dto.incrementalauth.CPPaymentIncrementalRequest;
import com.mgm.pd.cp.resortpayment.dto.incrementalauth.IncrementalAuthorizationRouterResponse;
import com.mgm.pd.cp.resortpayment.dto.opera.OperaResponse;
import com.mgm.pd.cp.resortpayment.model.Payment;
import com.mgm.pd.cp.resortpayment.service.router.RouterService;
import com.mgm.pd.cp.resortpayment.util.common.Converter;
import com.mgm.pd.cp.resortpayment.util.common.PaymentProcessingServiceHelper;
import feign.FeignException;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Optional;


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
     * @param incrementalRequest
     * @return response
     * @throws JsonProcessingException
     */
    @Override
    public ResponseEntity<GenericResponse> processIncrementalAuthorizationRequest(CPPaymentIncrementalRequest incrementalRequest) throws JsonProcessingException {
        Optional<Payment> optionalInitialPayment = findPaymentService.getPaymentDetails(incrementalRequest.getPropertyCode(), incrementalRequest.getResvNameID());
        if (optionalInitialPayment.isPresent()) {
            OperaResponse operaResponse;
            IncrementalAuthorizationRouterResponse irResponse = null;
            Payment payment;
            try{
                irResponse = routerService.sendIncrementalAuthorizationRequestToRouter(incrementalRequest, optionalInitialPayment.get().getIncrementalAuthInvoiceId());
            } catch(FeignException feignEx) {
                irResponse = serviceHelper.addCommentsForIncrementalAuthResponse(feignEx);
                throw feignEx;
            }
            finally {
                payment = savePaymentService.saveIncrementalAuthorizationPayment(incrementalRequest, irResponse);
            }
            operaResponse = converter.convert(payment);
            return response(operaResponse, HttpStatus.CREATED);
        }
        return initialPaymentIsMissing();
    }



    @Override
    @SneakyThrows
    public ResponseEntity<GenericResponse> authorizePayments(CPPaymentAuthorizationRequest cpPaymentAuthorizationRequest)  {
        OperaResponse operaResponse;
        AuthorizationRouterResponse authRouterResponse = null;
        Payment payment;

        try{
            authRouterResponse = routerService.sendAuthorizeRequestToRouter(cpPaymentAuthorizationRequest, 12345L );
        } catch(FeignException feignEx) {
            authRouterResponse = serviceHelper.addCommentsForAuthorizationAuthResponse(feignEx);
            throw feignEx;
        }
        finally {
            payment = savePaymentService.saveAuthPayment(cpPaymentAuthorizationRequest, authRouterResponse);
        }
        operaResponse = converter.convert(payment);
        return response(operaResponse, HttpStatus.CREATED);
    }


    /**
     *
     * @param captureRequest
     * @return response
     * @throws JsonProcessingException
     */
    @Override
    public ResponseEntity<GenericResponse> processCaptureRequest(CPPaymentCaptureRequest captureRequest) throws JsonProcessingException {
        Optional<Payment> optionalInitialPayment = findPaymentService.getPaymentDetails(captureRequest.getPropertyCode(), captureRequest.getResvNameID());
        if (optionalInitialPayment.isPresent()) {
            Payment initialPayment = optionalInitialPayment.get();
            OperaResponse operaCaptureResponse;
            Payment payment;
            CaptureRouterResponse crResponse = null;
            try{
                crResponse = routerService.sendCaptureRequestToRouter(captureRequest, initialPayment.getIncrementalAuthInvoiceId());
            } catch(FeignException feignEx) {
                crResponse = serviceHelper.addCommentsForCaptureResponse(feignEx);
                throw feignEx;
            } finally {
                payment = savePaymentService.saveCaptureAuthPayment(captureRequest, crResponse, initialPayment.getAuthTotalAmount());
            }
            operaCaptureResponse = converter.convert(payment);
            return response(operaCaptureResponse, HttpStatus.CREATED);
        }
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
        Optional<Payment> OptionalInitialPayment = findPaymentService.getPaymentDetails(cvRequest.getPropertyCode(), cvRequest.getResvNameID());
        if (OptionalInitialPayment.isPresent()) {
            OperaResponse operaCardVoidResponse;
            Payment payment;
            CardVoidRouterResponse cvrResponse = null;
            try{
                cvrResponse = routerService.sendCardVoidRequestToRouter(cvRequest, OptionalInitialPayment.get().getIncrementalAuthInvoiceId());
            } catch(FeignException feignEx) {
                cvrResponse = serviceHelper.addCommentsForCardVoidResponse(feignEx);
                throw feignEx;
            } finally {
                payment = savePaymentService.saveCardVoidAuthPayment(cvRequest, cvrResponse);
            }
            operaCardVoidResponse = converter.convert(payment);
            return response(operaCardVoidResponse, HttpStatus.CREATED);
        }
        return initialPaymentIsMissing();
    }

    private ResponseEntity<GenericResponse> initialPaymentIsMissing() {
        return response(new ErrorResponse(null, 422, "Initial Payment is missing",
                "Initial Payment is missing", null, null, null), HttpStatus.UNPROCESSABLE_ENTITY);
    }

    private <D> ResponseEntity<GenericResponse> response(D data, HttpStatus status) {
        return new ResponseEntity<>(GenericResponse.builder().data(data).build(), status);
    }
}
