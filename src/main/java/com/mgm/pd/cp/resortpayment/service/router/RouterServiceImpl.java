package com.mgm.pd.cp.resortpayment.service.router;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mgm.pd.cp.resortpayment.dto.authorize.AuthorizationRouterResponse;
import com.mgm.pd.cp.resortpayment.dto.authorize.CPPaymentAuthorizationRequest;
import com.mgm.pd.cp.resortpayment.dto.capture.CPPaymentCaptureRequest;
import com.mgm.pd.cp.resortpayment.dto.capture.CaptureRouterResponse;
import com.mgm.pd.cp.resortpayment.dto.cardvoid.CPPaymentCardVoidRequest;
import com.mgm.pd.cp.resortpayment.dto.cardvoid.CardVoidRouterResponse;
import com.mgm.pd.cp.resortpayment.dto.incrementalauth.CPPaymentIncrementalRequest;
import com.mgm.pd.cp.resortpayment.dto.incrementalauth.IncrementalAuthorizationRouterResponse;
import com.mgm.pd.cp.resortpayment.dto.router.RouterRequest;
import com.mgm.pd.cp.resortpayment.dto.router.RouterResponseJson;
import com.mgm.pd.cp.resortpayment.util.authorize.AuthorizeToRouterConverter;
import com.mgm.pd.cp.resortpayment.util.capture.CaptureToRouterConverter;
import com.mgm.pd.cp.resortpayment.util.cardvoid.VoidToRouterConverter;
import com.mgm.pd.cp.resortpayment.util.incremental.IncrementalToRouterConverter;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class RouterServiceImpl implements RouterService {
    private static final Logger logger = LogManager.getLogger(RouterServiceImpl.class);

    ObjectMapper mapper;
    RouterClient routerClient;
    IncrementalToRouterConverter incrementalToRouterConverter;
    AuthorizeToRouterConverter authorizeToRouterConverter;
    CaptureToRouterConverter captureToRouterConverter;
    VoidToRouterConverter voidToRouterConverter;
    @Override
    @Retry(name = "incrementalAuthorizationMessage")
    public IncrementalAuthorizationRouterResponse sendIncrementalAuthorizationRequestToRouter(CPPaymentIncrementalRequest incrementalRequest, Long incrementalAuthInvoiceId) throws JsonProcessingException {
        incrementalRequest.setIncrementalAuthInvoiceId(incrementalAuthInvoiceId);
        RouterRequest routerRequest = incrementalToRouterConverter.convert(incrementalRequest);
        logger.log(Level.DEBUG, "Attempting to send Message To Router for IncrementalAuthorizationRequest: " + routerRequest.getGatewayId());
        RouterResponseJson responseJson = routerClient.sendRequest(routerRequest);
        logger.log(Level.DEBUG, "Successfully Sent Message To Router for IncrementalAuthorizationRequest: " + routerRequest.getGatewayId());
        return mapper.readValue(responseJson.getResponseJson(), IncrementalAuthorizationRouterResponse.class);
    }

    @SneakyThrows
    @Retry(name = "authorizeMessage")
    public AuthorizationRouterResponse sendAuthorizeRequestToRouter(CPPaymentAuthorizationRequest authorizationRequest, Long incrementalAuthInvoiceId) throws JsonProcessingException {
        authorizationRequest.setIncrementalAuthInvoiceId(incrementalAuthInvoiceId);
        RouterRequest routerRequest = authorizeToRouterConverter.convert(authorizationRequest);
        RouterResponseJson responseJson = routerClient.sendRequest(routerRequest);
        return mapper.readValue(responseJson.getResponseJson(), AuthorizationRouterResponse.class);
    }

    @Override
    @Retry(name = "captureMessage")
    public CaptureRouterResponse sendCaptureRequestToRouter(CPPaymentCaptureRequest captureRequest, Long incrementalAuthInvoiceId) throws JsonProcessingException {
        captureRequest.setIncrementalAuthInvoiceId(incrementalAuthInvoiceId);
        RouterRequest routerRequest = captureToRouterConverter.convert(captureRequest);
        logger.log(Level.DEBUG, "Attempting to send Message To Router for CaptureRequest: " + routerRequest.getGatewayId());
        RouterResponseJson responseJson = routerClient.sendRequest(routerRequest);
        logger.log(Level.DEBUG, "Successfully Sent Message To Router for CaptureRequest: " + routerRequest.getGatewayId());
        return mapper.readValue(responseJson.getResponseJson(), CaptureRouterResponse.class);
    }

    @Override
    @Retry(name = "cardVoidMessage")
    public CardVoidRouterResponse sendCardVoidRequestToRouter(CPPaymentCardVoidRequest cvRequest, Long incrementalAuthInvoiceId) throws JsonProcessingException {
        cvRequest.setIncrementalAuthInvoiceId(incrementalAuthInvoiceId);
        RouterRequest routerRequest = voidToRouterConverter.convert(cvRequest);
        logger.log(Level.DEBUG, "Attempting to send Message To Router for CardVoidRequest: " + routerRequest.getGatewayId());
        RouterResponseJson responseJson = routerClient.sendRequest(routerRequest);
        logger.log(Level.DEBUG, "Successfully Sent Message To Router for CardVoidRequest: " + routerRequest.getGatewayId());
        return mapper.readValue(responseJson.getResponseJson(), CardVoidRouterResponse.class);
    }
}
