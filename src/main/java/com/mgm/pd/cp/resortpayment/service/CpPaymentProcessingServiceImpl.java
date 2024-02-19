package com.mgm.pd.cp.resortpayment.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mgm.pd.cp.resortpayment.constant.ApplicationConstants;
import com.mgm.pd.cp.resortpayment.dto.capture.CPPaymentCaptureRequest;
import com.mgm.pd.cp.resortpayment.dto.capture.CaptureRouterResponse;
import com.mgm.pd.cp.resortpayment.dto.cardvoid.CPPaymentCardVoidRequest;
import com.mgm.pd.cp.resortpayment.dto.cardvoid.CardVoidRouterResponse;
import com.mgm.pd.cp.resortpayment.dto.common.GenericResponse;
import com.mgm.pd.cp.resortpayment.dto.error.ErrorResponse;
import com.mgm.pd.cp.resortpayment.dto.exception.IntelligentRouterException;
import com.mgm.pd.cp.resortpayment.dto.incrementalauth.CPPaymentIncrementalRequest;
import com.mgm.pd.cp.resortpayment.dto.incrementalauth.IncrementalAuthorizationRouterResponse;
import com.mgm.pd.cp.resortpayment.dto.opera.OperaResponse;
import com.mgm.pd.cp.resortpayment.dto.router.RouterRequest;
import com.mgm.pd.cp.resortpayment.dto.router.RouterResponseJson;
import com.mgm.pd.cp.resortpayment.model.Payment;
import com.mgm.pd.cp.resortpayment.util.capture.CaptureToRouterConverter;
import com.mgm.pd.cp.resortpayment.util.cardvoid.VoidToRouterConverter;
import com.mgm.pd.cp.resortpayment.util.common.Converter;
import com.mgm.pd.cp.resortpayment.util.incremental.IncrementalToRouterConverter;
import feign.FeignException;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class CpPaymentProcessingServiceImpl implements CpPaymentProcessingService {
    ObjectMapper mapper;
    Converter converter;
    FindPaymentService findPaymentService;
    SavePaymentService savePaymentService;
    RouterClient routerClient;
    IncrementalToRouterConverter incrementalToRouterConverter;
    CaptureToRouterConverter captureToRouterConverter;
    VoidToRouterConverter voidToRouterConverter;

    @Override
    public ResponseEntity<GenericResponse> processIncrementalAuthorizationRequest(CPPaymentIncrementalRequest incrementalRequest) throws JsonProcessingException {
        Optional<Payment> optionalInitialPayment = findPaymentService.getPaymentDetails(incrementalRequest.getPropertyCode(), incrementalRequest.getResvNameID());
        if (optionalInitialPayment.isPresent()) {
            OperaResponse operaResponse;
            IncrementalAuthorizationRouterResponse irResponse = null;
            Payment payment;
            try{
                irResponse = sendIncrementalRequestToRouter(incrementalRequest, optionalInitialPayment.get().getIncrementalAuthInvoiceId());
            } catch(FeignException feignEx) {
                irResponse = addCommentsForIncrementalAuthResponse(feignEx);
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
    public ResponseEntity<GenericResponse> processCaptureRequest(CPPaymentCaptureRequest captureRequest) throws JsonProcessingException {
        Optional<Payment> optionalInitialPayment = findPaymentService.getPaymentDetails(captureRequest.getPropertyCode(), captureRequest.getResvNameID());
        if (optionalInitialPayment.isPresent()) {
            Payment initialPayment = optionalInitialPayment.get();
            OperaResponse operaCaptureResponse;
            Payment payment;
            CaptureRouterResponse crResponse = null;
            try{
                crResponse = sendCaptureRequestToRouter(captureRequest, initialPayment.getIncrementalAuthInvoiceId());
            } catch(FeignException feignEx) {
                crResponse = addCommentsForCaptureResponse(feignEx);
                throw feignEx;
            } finally {
                payment = savePaymentService.saveCaptureAuthPayment(captureRequest, crResponse, initialPayment.getAuthTotalAmount());
            }
            operaCaptureResponse = converter.convert(payment);
            return response(operaCaptureResponse, HttpStatus.CREATED);
        }
        return initialPaymentIsMissing();
    }

    @Override
    public ResponseEntity<GenericResponse> processCardVoidRequest(CPPaymentCardVoidRequest cvRequest) throws JsonProcessingException {
        Optional<Payment> OptionalInitialPayment = findPaymentService.getPaymentDetails(cvRequest.getPropertyCode(), cvRequest.getResvNameID());
        if (OptionalInitialPayment.isPresent()) {
            OperaResponse operaCardVoidResponse;
            Payment payment;
            CardVoidRouterResponse cvrResponse = null;
            try{
                cvrResponse = sendCardVoidRequestToRouter(cvRequest, OptionalInitialPayment.get().getIncrementalAuthInvoiceId());
            } catch(FeignException feignEx) {
                cvrResponse = addCommentsForCardVoidResponse(feignEx);
                throw feignEx;
            } finally {
                payment = savePaymentService.saveCardVoidAuthPayment(cvRequest, cvrResponse);
            }
            operaCardVoidResponse = converter.convert(payment);
            return response(operaCardVoidResponse, HttpStatus.CREATED);
        }
        return initialPaymentIsMissing();
    }

    private IncrementalAuthorizationRouterResponse sendIncrementalRequestToRouter(CPPaymentIncrementalRequest incrementalRequest, Long incrementalAuthInvoiceId) throws JsonProcessingException {
        incrementalRequest.setIncrementalAuthInvoiceId(incrementalAuthInvoiceId);
        RouterRequest routerRequest = incrementalToRouterConverter.convert(incrementalRequest);
        RouterResponseJson responseJson = routerClient.sendRequest(routerRequest);
        return mapper.readValue(responseJson.getResponseJson(), IncrementalAuthorizationRouterResponse.class);
    }

    private CaptureRouterResponse sendCaptureRequestToRouter(CPPaymentCaptureRequest captureRequest, Long incrementalAuthInvoiceId) throws JsonProcessingException {
        captureRequest.setIncrementalAuthInvoiceId(incrementalAuthInvoiceId);
        RouterRequest routerRequest = captureToRouterConverter.convert(captureRequest);
        RouterResponseJson responseJson = routerClient.sendRequest(routerRequest);
        return mapper.readValue(responseJson.getResponseJson(), CaptureRouterResponse.class);
    }

    private CardVoidRouterResponse sendCardVoidRequestToRouter(CPPaymentCardVoidRequest voidRequest, Long incrementalAuthInvoiceId) throws JsonProcessingException {
        voidRequest.setIncrementalAuthInvoiceId(incrementalAuthInvoiceId);
        RouterRequest routerRequest = voidToRouterConverter.convert(voidRequest);
        RouterResponseJson responseJson = routerClient.sendRequest(routerRequest);
        return mapper.readValue(responseJson.getResponseJson(), CardVoidRouterResponse.class);
    }

    private ResponseEntity<GenericResponse> initialPaymentIsMissing() {
        return response(new ErrorResponse(null, 422, "Initial Payment is missing",
                "Initial Payment is missing", null, null, null), HttpStatus.UNPROCESSABLE_ENTITY);
    }

    private static CardVoidRouterResponse addCommentsForCardVoidResponse(FeignException feignEx) throws JsonProcessingException {
        String contentedUTF8 = feignEx.contentUTF8();
        CardVoidRouterResponse cvrResponse;
        if (!contentedUTF8.isBlank()) {
            IntelligentRouterException irEx = new ObjectMapper().readValue(contentedUTF8, IntelligentRouterException.class);
            //To get the error message from Router to save in comments column in Payment table
            cvrResponse = CardVoidRouterResponse.builder().comments(irEx.getMessage()).build();
        } else {
            cvrResponse = CardVoidRouterResponse.builder().comments(ApplicationConstants.INTELLIGENT_ROUTER_CONNECTION_EXCEPTION_MESSAGE).build();
        }
        return cvrResponse;
    }

    private CaptureRouterResponse addCommentsForCaptureResponse(FeignException feignEx) throws JsonProcessingException {
        String contentedUTF8 = feignEx.contentUTF8();
        CaptureRouterResponse crResponse;
        if (!contentedUTF8.isBlank()) {
            IntelligentRouterException irEx = new ObjectMapper().readValue(contentedUTF8, IntelligentRouterException.class);
            //To get the error message from Router to save in comments column in Payment table
            crResponse = CaptureRouterResponse.builder().comments(irEx.getMessage()).build();
        } else {
            crResponse = CaptureRouterResponse.builder().comments(ApplicationConstants.INTELLIGENT_ROUTER_CONNECTION_EXCEPTION_MESSAGE).build();
        }
        return crResponse;
    }

    private IncrementalAuthorizationRouterResponse addCommentsForIncrementalAuthResponse(FeignException feignEx) throws JsonProcessingException {
        String contentedUTF8 = feignEx.contentUTF8();
        IncrementalAuthorizationRouterResponse irResponse;
        if (!contentedUTF8.isBlank()) {
            IntelligentRouterException irEx = new ObjectMapper().readValue(contentedUTF8, IntelligentRouterException.class);
            //To get the error message from Router to save in comments column in Payment table
            irResponse = IncrementalAuthorizationRouterResponse.builder().comments(irEx.getMessage()).build();
        } else {
            irResponse = IncrementalAuthorizationRouterResponse.builder().comments(ApplicationConstants.INTELLIGENT_ROUTER_CONNECTION_EXCEPTION_MESSAGE).build();
        }
        return irResponse;
    }

    private <D> ResponseEntity<GenericResponse> response(D data, HttpStatus status) {
        return new ResponseEntity<>(GenericResponse.builder().data(data).build(), status);
    }
}
