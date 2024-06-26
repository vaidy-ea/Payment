package com.mgm.pd.cp.resortpayment.service.router;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mgm.pd.cp.payment.common.dto.*;
import com.mgm.pd.cp.payment.common.model.Payment;
import com.mgm.pd.cp.resortpayment.dto.authorize.AuthorizationRouterResponse;
import com.mgm.pd.cp.resortpayment.dto.capture.CaptureRouterResponse;
import com.mgm.pd.cp.resortpayment.dto.cardvoid.CardVoidRouterResponse;
import com.mgm.pd.cp.resortpayment.dto.incrementalauth.IncrementalAuthorizationRouterResponse;
import com.mgm.pd.cp.resortpayment.dto.refund.RefundRouterResponse;
import com.mgm.pd.cp.resortpayment.dto.router.RouterRequest;
import com.mgm.pd.cp.resortpayment.dto.router.RouterResponseJson;
import com.mgm.pd.cp.resortpayment.util.authorize.AuthorizeToRouterConverter;
import com.mgm.pd.cp.resortpayment.util.capture.CaptureToRouterConverter;
import com.mgm.pd.cp.resortpayment.util.cardvoid.VoidToRouterConverter;
import com.mgm.pd.cp.resortpayment.util.incremental.IncrementalToRouterConverter;
import com.mgm.pd.cp.resortpayment.util.refund.RefundToRouterConverter;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * This class is responsible for implementing the methods
 * declared in RouterHelper
 * and to provide complete implementation for those methods.
 */
@Service
@AllArgsConstructor
public class RouterHelperImpl implements RouterHelper {
    private static final Logger logger = LogManager.getLogger(RouterHelperImpl.class);

    private ObjectMapper mapper;
    private RouterClient routerClient;
    private IncrementalToRouterConverter incrementalToRouterConverter;
    private AuthorizeToRouterConverter authorizeToRouterConverter;
    private CaptureToRouterConverter captureToRouterConverter;
    private VoidToRouterConverter voidToRouterConverter;
    private RefundToRouterConverter refundToRouterConverter;

    /**
     * This method is responsible for taking IncrementalAuth Request and send it to Intelligent Router(IR)
     * and return response received form IR
     * Retry (Resilience4J) mechanism is used to try sending request to IR as per application properties file
     *
     * @param request: request to convert and send to Router
     * @param initialPayment: initialPayment
     * @param headers: Request Headers
     */
    @Override
    public IncrementalAuthorizationRouterResponse sendIncrementalAuthorizationRequestToRouter(CPPaymentIncrementalAuthRequest request,
                                                                                              Payment initialPayment, HttpHeaders headers) throws JsonProcessingException {
        request.setReferenceId(Objects.nonNull(initialPayment) ? initialPayment.getPaymentId() : null);
        //converting request to IR compatible
        RouterRequest routerRequest = incrementalToRouterConverter.convert(request);
        logger.log(Level.INFO, "Attempting to send message to Intelligent Router for IncrementalAuthorizationRequest: {}", routerRequest);
        //sending request to IR using Feign Client
        RouterResponseJson responseJson = routerClient.sendRequest(headers, routerRequest);
        logger.log(Level.INFO, "Successfully Received message from Intelligent Router for IncrementalAuthorizationRequest: {}", responseJson);
        //using object Mapper to convert response received from IR
        return mapper.readValue(responseJson.getResponseJson(), IncrementalAuthorizationRouterResponse.class);
    }

    /**
     * This method is responsible for taking Authorization Request and send it to Intelligent Router(IR)
     * and return response received form IR
     * Retry (Resilience4J) mechanism is used to try sending request to IR as per application properties file
     *
     * @param request : request to convert and send to Router
     * @param headers : Request Headers
     */
    @Override
    public AuthorizationRouterResponse sendAuthorizeRequestToRouter(CPPaymentAuthorizationRequest request, HttpHeaders headers) throws JsonProcessingException {
        //converting request to IR compatible
        RouterRequest routerRequest = authorizeToRouterConverter.convert(request);
        logger.log(Level.INFO, "Attempting to send message to Intelligent Router for AuthorizeRequest: {}", routerRequest);
        //sending request to IR using Feign Client
        RouterResponseJson responseJson = routerClient.sendRequest(headers, routerRequest);
        logger.log(Level.INFO, "Successfully Received message from Intelligent Router for AuthorizeRequest: {}", responseJson);
        //using object Mapper to convert response received from IR
        return mapper.readValue(responseJson.getResponseJson(), AuthorizationRouterResponse.class);
    }

    /**
     * This method is responsible for taking Capture Request and send it to Intelligent Router(IR)
     * and return response received form IR
     * Retry (Resilience4J) mechanism is used to try sending request to IR as per application properties file
     *
     * @param request: request to convert and send to Router
     * @param initialPayment: initialPayment
     * @param headers: Request Headers
     */
    @Override
    public CaptureRouterResponse sendCaptureRequestToRouter(CPPaymentCaptureRequest request, Payment initialPayment, HttpHeaders headers) throws JsonProcessingException {
        request.setReferenceId(Objects.nonNull(initialPayment) ? initialPayment.getPaymentId() : null);
        //converting request to IR compatible
        RouterRequest routerRequest = captureToRouterConverter.convert(request);
        logger.log(Level.INFO, "Attempting to send Message To Intelligent Router for CaptureRequest: {}", routerRequest);
        //sending request to IR using Feign Client
        RouterResponseJson responseJson = routerClient.sendRequest(headers, routerRequest);
        logger.log(Level.INFO, "Successfully Received Message from Intelligent Router for CaptureRequest: {}", responseJson);
        //using object Mapper to convert response received from IR
        return mapper.readValue(responseJson.getResponseJson(), CaptureRouterResponse.class);
    }

    /**
     * This method is responsible for taking Card Void Request and send it to Intelligent Router(IR)
     * and return response received form IR
     * Retry (Resilience4J) mechanism is used to try sending request to IR as per application properties file
     *
     * @param request: request to convert and send to Router
     * @param initialPayment: initialPayment
     * @param headers: Request Headers
     */
    @Override
    public CardVoidRouterResponse sendCardVoidRequestToRouter(CPPaymentCardVoidRequest request, Payment initialPayment, HttpHeaders headers) throws JsonProcessingException {
        request.setReferenceId(Objects.nonNull(initialPayment) ? initialPayment.getPaymentId() : null);
        //converting request to IR compatible
        RouterRequest routerRequest = voidToRouterConverter.convert(request);
        logger.log(Level.INFO, "Attempting to send Message To Intelligent Router for CardVoidRequest: {}", routerRequest);
        //sending request to IR using Feign Client
        RouterResponseJson responseJson = routerClient.sendRequest(headers, routerRequest);
        logger.log(Level.INFO, "Successfully Received Message from Intelligent Router for CardVoidRequest: {}", responseJson);
        //using object Mapper to convert response received from IR
        return mapper.readValue(responseJson.getResponseJson(), CardVoidRouterResponse.class);
    }

    /**
     * This method is responsible for taking Refund Request and send it to Intelligent Router(IR)
     * and return response received form IR
     * Retry (Resilience4J) mechanism is used to try sending request to IR as per application properties file
     *
     * @param request : request to convert and send to Router
     * @param headers : Request Headers
     */
    @Override
    public RefundRouterResponse sendRefundRequestToRouter(CPPaymentRefundRequest request, HttpHeaders headers) throws JsonProcessingException {
        //converting request to IR compatible
        RouterRequest routerRequest = refundToRouterConverter.convert(request);
        logger.log(Level.INFO, "Attempting to send Message To Intelligent Router for Refund Request: {}", routerRequest);
        //sending request to IR using Feign Client
        RouterResponseJson responseJson = routerClient.sendRequest(headers, routerRequest);
        logger.log(Level.INFO, "Successfully Received Message from Intelligent Router for Refund Request: {}", responseJson);
        //using object Mapper to convert response received from IR
        return mapper.readValue(responseJson.getResponseJson(), RefundRouterResponse.class);
    }
}
