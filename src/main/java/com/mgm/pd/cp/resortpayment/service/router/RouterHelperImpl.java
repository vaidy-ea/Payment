package com.mgm.pd.cp.resortpayment.service.router;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import com.mgm.pd.cp.resortpayment.dto.router.RouterRequest;
import com.mgm.pd.cp.resortpayment.dto.router.RouterResponseJson;
import com.mgm.pd.cp.resortpayment.util.authorize.AuthorizeToRouterConverter;
import com.mgm.pd.cp.resortpayment.util.capture.CaptureToRouterConverter;
import com.mgm.pd.cp.resortpayment.util.cardvoid.VoidToRouterConverter;
import com.mgm.pd.cp.resortpayment.util.incremental.IncrementalToRouterConverter;
import com.mgm.pd.cp.resortpayment.util.refund.RefundToRouterConverter;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

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
     * @param incrementalAuthInvoiceId: authInvoiceId
     */
    @Override
    @Retry(name = "incrementalAuthorizationMessage")
    public IncrementalAuthorizationRouterResponse sendIncrementalAuthorizationRequestToRouter(CPPaymentIncrementalAuthRequest request,
                                                                                            Long incrementalAuthInvoiceId) throws JsonProcessingException {
        request.setIncrementalAuthInvoiceId(incrementalAuthInvoiceId);
        //converting request to IR compatible
        RouterRequest routerRequest = incrementalToRouterConverter.convert(request);
        logger.log(Level.DEBUG, "Attempting to send message to Intelligent Router for IncrementalAuthorizationRequest");
        //sending request to IR using Feign Client
        RouterResponseJson responseJson = routerClient.sendRequest(routerRequest);
        logger.log(Level.DEBUG, "Successfully Sent message to Intelligent Router for IncrementalAuthorizationRequest");
        //using object Mapper to convert response received from IR
        return mapper.readValue(responseJson.getResponseJson(), IncrementalAuthorizationRouterResponse.class);
    }

    /**
     * This method is responsible for taking Authorization Request and send it to Intelligent Router(IR)
     * and return response received form IR
     * Retry (Resilience4J) mechanism is used to try sending request to IR as per application properties file
     *
     * @param request: request to convert and send to Router
     * @param incrementalAuthInvoiceId: authInvoiceId
     */
    @Override
    @Retry(name = "authorizeMessage")
    public AuthorizationRouterResponse sendAuthorizeRequestToRouter(CPPaymentAuthorizationRequest request, Long incrementalAuthInvoiceId) throws JsonProcessingException {
        request.setIncrementalAuthInvoiceId(incrementalAuthInvoiceId);
        //converting request to IR compatible
        RouterRequest routerRequest = authorizeToRouterConverter.convert(request);
        logger.log(Level.DEBUG, "Attempting to send message to Intelligent Router for AuthorizeRequest");
        //sending request to IR using Feign Client
        RouterResponseJson responseJson = routerClient.sendRequest(routerRequest);
        logger.log(Level.DEBUG, "Successfully Sent message to Intelligent Router for AuthorizeRequest");
        //using object Mapper to convert response received from IR
        return mapper.readValue(responseJson.getResponseJson(), AuthorizationRouterResponse.class);
    }

    /**
     * This method is responsible for taking Capture Request and send it to Intelligent Router(IR)
     * and return response received form IR
     * Retry (Resilience4J) mechanism is used to try sending request to IR as per application properties file
     *
     * @param request: request to convert and send to Router
     * @param incrementalAuthInvoiceId: authInvoiceId
     */
    @Override
    @Retry(name = "captureMessage")
    public CaptureRouterResponse sendCaptureRequestToRouter(CPPaymentCaptureRequest request, Long incrementalAuthInvoiceId) throws JsonProcessingException {
        request.setIncrementalAuthInvoiceId(incrementalAuthInvoiceId);
        //converting request to IR compatible
        RouterRequest routerRequest = captureToRouterConverter.convert(request);
        logger.log(Level.DEBUG, "Attempting to send Message To Intelligent Router for CaptureRequest");
        //sending request to IR using Feign Client
        RouterResponseJson responseJson = routerClient.sendRequest(routerRequest);
        logger.log(Level.DEBUG, "Successfully Sent Message To Intelligent Router for CaptureRequest");
        //using object Mapper to convert response received from IR
        return mapper.readValue(responseJson.getResponseJson(), CaptureRouterResponse.class);
    }

    /**
     * This method is responsible for taking Card Void Request and send it to Intelligent Router(IR)
     * and return response received form IR
     * Retry (Resilience4J) mechanism is used to try sending request to IR as per application properties file
     *
     * @param request: request to convert and send to Router
     * @param incrementalAuthInvoiceId: authInvoiceId
     */
    @Override
    @Retry(name = "cardVoidMessage")
    public CardVoidRouterResponse sendCardVoidRequestToRouter(CPPaymentCardVoidRequest request, Long incrementalAuthInvoiceId) throws JsonProcessingException {
        request.setIncrementalAuthInvoiceId(incrementalAuthInvoiceId);
        //converting request to IR compatible
        RouterRequest routerRequest = voidToRouterConverter.convert(request);
        logger.log(Level.DEBUG, "Attempting to send Message To Intelligent Router for CardVoidRequest");
        //sending request to IR using Feign Client
        RouterResponseJson responseJson = routerClient.sendRequest(routerRequest);
        logger.log(Level.DEBUG, "Successfully Sent Message To Intelligent Router for CardVoidRequest");
        //using object Mapper to convert response received from IR
        return mapper.readValue(responseJson.getResponseJson(), CardVoidRouterResponse.class);
    }

    /**
     * This method is responsible for taking Refund Request and send it to Intelligent Router(IR)
     * and return response received form IR
     * Retry (Resilience4J) mechanism is used to try sending request to IR as per application properties file
     *
     * @param request: request to convert and send to Router
     * @param incrementalAuthInvoiceId: authInvoiceId
     */
    @Override
    @Retry(name = "refundMessage")
    public RefundRouterResponse sendRefundRequestToRouter(CPPaymentRefundRequest request, Long incrementalAuthInvoiceId) throws JsonProcessingException {
        request.setIncrementalAuthInvoiceId(incrementalAuthInvoiceId);
        //converting request to IR compatible
        RouterRequest routerRequest = refundToRouterConverter.convert(request);
        logger.log(Level.DEBUG, "Attempting to send Message To Intelligent Router for Refund Request");
        //sending request to IR using Feign Client
        RouterResponseJson responseJson = routerClient.sendRequest(routerRequest);
        logger.log(Level.DEBUG, "Successfully Sent Message To Intelligent Router for Refund Request");
        //using object Mapper to convert response received from IR
        return mapper.readValue(responseJson.getResponseJson(), RefundRouterResponse.class);
    }
}
