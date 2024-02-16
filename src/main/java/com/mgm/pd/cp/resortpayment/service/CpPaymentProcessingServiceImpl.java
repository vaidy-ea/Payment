package com.mgm.pd.cp.resortpayment.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mgm.pd.cp.resortpayment.dto.capture.CPPaymentCaptureRequest;
import com.mgm.pd.cp.resortpayment.dto.capture.CaptureRouterResponse;
import com.mgm.pd.cp.resortpayment.dto.cardvoid.CPPaymentVoidRequest;
import com.mgm.pd.cp.resortpayment.dto.cardvoid.VoidRouterResponse;
import com.mgm.pd.cp.resortpayment.dto.common.GenericResponse;
import com.mgm.pd.cp.resortpayment.dto.common.OperaResponse;
import com.mgm.pd.cp.resortpayment.dto.incrementalauth.CPPaymentIncrementalRequest;
import com.mgm.pd.cp.resortpayment.dto.incrementalauth.IncrementalRouterResponse;
import com.mgm.pd.cp.resortpayment.dto.router.RouterRequest;
import com.mgm.pd.cp.resortpayment.dto.router.RouterResponseJson;
import com.mgm.pd.cp.resortpayment.model.Payment;
import com.mgm.pd.cp.resortpayment.util.capture.CaptureRouterToOperaConverter;
import com.mgm.pd.cp.resortpayment.util.capture.CaptureToRouterConverter;
import com.mgm.pd.cp.resortpayment.util.cardvoid.VoidRouterToOperaConverter;
import com.mgm.pd.cp.resortpayment.util.cardvoid.VoidToRouterConverter;
import com.mgm.pd.cp.resortpayment.util.incremental.IncrementalRouterToOperaConverter;
import com.mgm.pd.cp.resortpayment.util.incremental.IncrementalToRouterConverter;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.mgm.pd.cp.resortpayment.constant.ApplicationConstants.*;

@Service
@AllArgsConstructor
public class CpPaymentProcessingServiceImpl implements CpPaymentProcessingService {
    ObjectMapper mapper;
    IncrementalRouterToOperaConverter incrementalRouterToOperaConverter;
    CaptureRouterToOperaConverter captureRouterToOperaConverter;
    VoidRouterToOperaConverter voidRouterToOperaConverter;
    FindPaymentService findPaymentService;
    SavePaymentService savePaymentService;
    RouterClient routerClient;
    IncrementalToRouterConverter incrementalToRouterConverter;
    CaptureToRouterConverter captureToRouterConverter;
    VoidToRouterConverter voidToRouterConverter;

    @Override
    public ResponseEntity<GenericResponse> processIncrementalRequest(CPPaymentIncrementalRequest incrementalRequest) throws JsonProcessingException {
        Optional<Payment> lastPayment = findPaymentService.getPaymentDetails(incrementalRequest.getPropertyCode(), incrementalRequest.getResvNameID());
        if (lastPayment.isPresent()) {
            IncrementalRouterResponse irResponse = sendIncrementalRequestToRouter(incrementalRequest, lastPayment.get().getIncrementalAuthInvoiceId());
            Payment payment = savePaymentService.saveIncrementalAuthPayment(incrementalRequest, irResponse);
            OperaResponse operaResponse = incrementalRouterToOperaConverter.convert(payment, irResponse);
            return response(SUCCESS_MESSAGE, SUCCESS_CODE, operaResponse, HttpStatus.CREATED);
        }
        return response(FAILURE_MESSAGE, FAILURE_CODE, null, HttpStatus.BAD_REQUEST);
    }

    @Override
    public ResponseEntity<GenericResponse> processCaptureRequest(CPPaymentCaptureRequest captureRequest) throws JsonProcessingException {
        Optional<Payment> lastPayment = findPaymentService.getPaymentDetails(captureRequest.getPropertyCode(), captureRequest.getResvNameID());
        if (lastPayment.isPresent()) {
            Payment initialPayment = lastPayment.get();
            CaptureRouterResponse irResponse = sendCaptureRequestToRouter(captureRequest, initialPayment.getIncrementalAuthInvoiceId());
            Payment payment = savePaymentService.saveCaptureAuthPayment(captureRequest, irResponse, initialPayment.getAuthTotalAmount());
            OperaResponse operaResponse = captureRouterToOperaConverter.convert(payment, irResponse);
            return response(SUCCESS_MESSAGE, SUCCESS_CODE, operaResponse, HttpStatus.CREATED);
        }
        return response(FAILURE_MESSAGE, FAILURE_CODE, null, HttpStatus.BAD_REQUEST);
    }

    @Override
    public ResponseEntity<GenericResponse> processVoidRequest(CPPaymentVoidRequest voidRequest) throws JsonProcessingException {
        Optional<Payment> lastPayment = findPaymentService.getPaymentDetails(voidRequest.getPropertyCode(), voidRequest.getResvNameID());
        if (lastPayment.isPresent()) {
            VoidRouterResponse vrResponse = sendVoidRequestToRouter(voidRequest, lastPayment.get().getIncrementalAuthInvoiceId());
            Payment payment = savePaymentService.saveVoidAuthPayment(voidRequest, vrResponse);
            OperaResponse operaResponse = voidRouterToOperaConverter.convert(payment, vrResponse);
            return response(SUCCESS_MESSAGE, SUCCESS_CODE, operaResponse, HttpStatus.CREATED);
        }
        return response(FAILURE_MESSAGE, FAILURE_CODE, null, HttpStatus.BAD_REQUEST);
    }

    private <D> ResponseEntity<GenericResponse> response(String message, String code, D data, HttpStatus status) {
        return new ResponseEntity<>(GenericResponse.builder()
                .message(message).code(code).data(data)
                .build(), status);
    }

    private IncrementalRouterResponse sendIncrementalRequestToRouter(CPPaymentIncrementalRequest incrementalRequest, Long incrementalAuthInvoiceId) throws JsonProcessingException {
        incrementalRequest.setIncrementalAuthInvoiceId(incrementalAuthInvoiceId);
        RouterRequest routerRequest = incrementalToRouterConverter.convert(incrementalRequest);
        RouterResponseJson responseJson = routerClient.sendRequest(routerRequest);
        return mapper.readValue(responseJson.getResponseJson(), IncrementalRouterResponse.class);
    }

    private CaptureRouterResponse sendCaptureRequestToRouter(CPPaymentCaptureRequest captureRequest, Long incrementalAuthInvoiceId) throws JsonProcessingException {
        captureRequest.setIncrementalAuthInvoiceId(incrementalAuthInvoiceId);
        RouterRequest routerRequest = captureToRouterConverter.convert(captureRequest);
        RouterResponseJson responseJson = routerClient.sendRequest(routerRequest);
        return mapper.readValue(responseJson.getResponseJson(), CaptureRouterResponse.class);
    }

    private VoidRouterResponse sendVoidRequestToRouter(CPPaymentVoidRequest voidRequest, Long incrementalAuthInvoiceId) throws JsonProcessingException {
        voidRequest.setIncrementalAuthInvoiceId(incrementalAuthInvoiceId);
        RouterRequest routerRequest = voidToRouterConverter.convert(voidRequest);
        RouterResponseJson responseJson = routerClient.sendRequest(routerRequest);
        return mapper.readValue(responseJson.getResponseJson(), VoidRouterResponse.class);
    }
}
