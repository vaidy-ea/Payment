package com.mgm.pd.cp.resortpayment.util.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mgm.pd.cp.resortpayment.dto.authorize.AuthorizationRouterResponse;
import com.mgm.pd.cp.resortpayment.dto.capture.CaptureRouterResponse;
import com.mgm.pd.cp.resortpayment.dto.cardvoid.CardVoidRouterResponse;
import com.mgm.pd.cp.resortpayment.dto.exception.IntelligentRouterException;
import com.mgm.pd.cp.resortpayment.dto.incrementalauth.IncrementalAuthorizationRouterResponse;
import feign.FeignException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import static com.mgm.pd.cp.payment.common.constant.ApplicationConstants.INTELLIGENT_ROUTER_CONNECTION_EXCEPTION_MESSAGE;

@Component
@AllArgsConstructor
public class PaymentProcessingServiceHelper {
    ObjectMapper mapper;
    public IncrementalAuthorizationRouterResponse addCommentsForIncrementalAuthResponse(FeignException feignEx) throws JsonProcessingException {
        String contentedUTF8 = feignEx.contentUTF8();
        IncrementalAuthorizationRouterResponse irResponse;
        if (!contentedUTF8.isBlank()) {
            IntelligentRouterException irEx = mapper.readValue(contentedUTF8, IntelligentRouterException.class);
            //To get the error message from Router to save in comments column in Payment table
            irResponse = IncrementalAuthorizationRouterResponse.builder().comments(irEx.getMessage()).build();
        } else {
            irResponse = IncrementalAuthorizationRouterResponse.builder().comments(INTELLIGENT_ROUTER_CONNECTION_EXCEPTION_MESSAGE).build();
        }
        return irResponse;
    }

    public AuthorizationRouterResponse addCommentsForAuthorizationAuthResponse(FeignException feignEx) throws JsonProcessingException {
        String contentedUTF8 = feignEx.contentUTF8();
        AuthorizationRouterResponse authorizationRouterResponse;
        if (!contentedUTF8.isBlank()) {
            IntelligentRouterException irEx = mapper.readValue(contentedUTF8, IntelligentRouterException.class);
            //To get the error message from Router to save in comments column in Payment table
            authorizationRouterResponse = AuthorizationRouterResponse.builder().comments(irEx.getMessage()).build();
        } else {
            authorizationRouterResponse = AuthorizationRouterResponse.builder().comments(INTELLIGENT_ROUTER_CONNECTION_EXCEPTION_MESSAGE).build();
        }
        return authorizationRouterResponse;
    }

    public CaptureRouterResponse addCommentsForCaptureResponse(FeignException feignEx) throws JsonProcessingException {
        String contentedUTF8 = feignEx.contentUTF8();
        CaptureRouterResponse crResponse;
        if (!contentedUTF8.isBlank()) {
            IntelligentRouterException irEx = mapper.readValue(contentedUTF8, IntelligentRouterException.class);
            //To get the error message from Router to save in comments column in Payment table
            crResponse = CaptureRouterResponse.builder().comments(irEx.getMessage()).build();
        } else {
            crResponse = CaptureRouterResponse.builder().comments(INTELLIGENT_ROUTER_CONNECTION_EXCEPTION_MESSAGE).build();
        }
        return crResponse;
    }

    public CardVoidRouterResponse addCommentsForCardVoidResponse(FeignException feignEx) throws JsonProcessingException {
        String contentedUTF8 = feignEx.contentUTF8();
        CardVoidRouterResponse cvrResponse;
        if (!contentedUTF8.isBlank()) {
            IntelligentRouterException irEx = mapper.readValue(contentedUTF8, IntelligentRouterException.class);
            //To get the error message from Router to save in comments column in Payment table
            cvrResponse = CardVoidRouterResponse.builder().comments(irEx.getMessage()).build();
        } else {
            cvrResponse = CardVoidRouterResponse.builder().comments(INTELLIGENT_ROUTER_CONNECTION_EXCEPTION_MESSAGE).build();
        }
        return cvrResponse;
    }
}
