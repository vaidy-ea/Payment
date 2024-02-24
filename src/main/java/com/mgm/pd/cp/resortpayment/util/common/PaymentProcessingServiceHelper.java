package com.mgm.pd.cp.resortpayment.util.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mgm.pd.cp.payment.common.dto.ErrorResponse;
import com.mgm.pd.cp.resortpayment.dto.Hotel;
import com.mgm.pd.cp.resortpayment.dto.Ticket;
import com.mgm.pd.cp.resortpayment.dto.authorize.AuthorizationRouterResponse;
import com.mgm.pd.cp.resortpayment.dto.authorize.CPPaymentAuthorizationRequest;
import com.mgm.pd.cp.resortpayment.dto.capture.CPPaymentCaptureRequest;
import com.mgm.pd.cp.resortpayment.dto.capture.CaptureRouterResponse;
import com.mgm.pd.cp.resortpayment.dto.cardvoid.CardVoidRouterResponse;
import com.mgm.pd.cp.resortpayment.dto.incrementalauth.CPPaymentIncrementalAuthRequest;
import com.mgm.pd.cp.resortpayment.dto.incrementalauth.IncrementalAuthorizationRouterResponse;
import com.mgm.pd.cp.resortpayment.dto.refund.CPPaymentRefundRequest;
import com.mgm.pd.cp.resortpayment.dto.refund.RefundRouterResponse;
import feign.FeignException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;

import static com.mgm.pd.cp.payment.common.constant.ApplicationConstants.INTELLIGENT_ROUTER_CONNECTION_EXCEPTION_MESSAGE;

@Component
@AllArgsConstructor
public class PaymentProcessingServiceHelper {
    ObjectMapper mapper;
    public IncrementalAuthorizationRouterResponse addCommentsForIncrementalAuthResponse(FeignException feignEx) throws JsonProcessingException {
        String contentedUTF8 = feignEx.contentUTF8();
        IncrementalAuthorizationRouterResponse irResponse;
        if (!contentedUTF8.isBlank()) {
            ErrorResponse irEx = mapper.readValue(contentedUTF8, ErrorResponse.class);
            //To get the error message from Router to save in comments column in Payment table
            irResponse = IncrementalAuthorizationRouterResponse.builder().comments(irEx.getDetail()).build();
        } else {
            irResponse = IncrementalAuthorizationRouterResponse.builder().comments(INTELLIGENT_ROUTER_CONNECTION_EXCEPTION_MESSAGE).build();
        }
        return irResponse;
    }

    public AuthorizationRouterResponse addCommentsForAuthorizationAuthResponse(FeignException feignEx) throws JsonProcessingException {
        String contentedUTF8 = feignEx.contentUTF8();
        AuthorizationRouterResponse authorizationRouterResponse;
        if (!contentedUTF8.isBlank()) {
            ErrorResponse irEx = mapper.readValue(contentedUTF8, ErrorResponse.class);
            //To get the error message from Router to save in comments column in Payment table
            authorizationRouterResponse = AuthorizationRouterResponse.builder().comments(irEx.getDetail()).build();
        } else {
            authorizationRouterResponse = AuthorizationRouterResponse.builder().comments(INTELLIGENT_ROUTER_CONNECTION_EXCEPTION_MESSAGE).build();
        }
        return authorizationRouterResponse;
    }

    public CaptureRouterResponse addCommentsForCaptureResponse(FeignException feignEx) throws JsonProcessingException {
        String contentedUTF8 = feignEx.contentUTF8();
        CaptureRouterResponse crResponse;
        if (!contentedUTF8.isBlank()) {
            ErrorResponse irEx = mapper.readValue(contentedUTF8, ErrorResponse.class);
            //To get the error message from Router to save in comments column in Payment table
            crResponse = CaptureRouterResponse.builder().comments(irEx.getDetail()).build();
        } else {
            crResponse = CaptureRouterResponse.builder().comments(INTELLIGENT_ROUTER_CONNECTION_EXCEPTION_MESSAGE).build();
        }
        return crResponse;
    }

    public CardVoidRouterResponse addCommentsForCardVoidResponse(FeignException feignEx) throws JsonProcessingException {
        String contentedUTF8 = feignEx.contentUTF8();
        CardVoidRouterResponse cvrResponse;
        if (!contentedUTF8.isBlank()) {
            ErrorResponse irEx = mapper.readValue(contentedUTF8, ErrorResponse.class);
            //To get the error message from Router to save in comments column in Payment table
            cvrResponse = CardVoidRouterResponse.builder().comments(irEx.getDetail()).build();
        } else {
            cvrResponse = CardVoidRouterResponse.builder().comments(INTELLIGENT_ROUTER_CONNECTION_EXCEPTION_MESSAGE).build();
        }
        return cvrResponse;
    }

    public RefundRouterResponse addCommentsForRefundResponse(FeignException feignEx) throws JsonProcessingException {
        String contentedUTF8 = feignEx.contentUTF8();
        RefundRouterResponse cvrResponse;
        if (!contentedUTF8.isBlank()) {
            ErrorResponse irEx = mapper.readValue(contentedUTF8, ErrorResponse.class);
            //To get the error message from Router to save in comments column in Payment table
            cvrResponse = RefundRouterResponse.builder().comments(irEx.getDetail()).build();
        } else {
            cvrResponse = RefundRouterResponse.builder().comments(INTELLIGENT_ROUTER_CONNECTION_EXCEPTION_MESSAGE).build();
        }
        return cvrResponse;
    }

    public String getValueByName(CPPaymentIncrementalAuthRequest request, String getterName) {
        Object data = request.getTransactionDetails().getSaleItem().getSaleDetails();
        return getValue(getterName, data);
    }

    private static String getValue(String getterName, Object data) {
        String property = null;
        try {
            Ticket ticket = new ObjectMapper().convertValue(data, Ticket.class);
            if ("roomNumber".equals(getterName)) {
                getterName = "ticketNumber";
            }
            property = (String) new PropertyDescriptor(getterName, Ticket.class).getReadMethod().invoke(ticket);
        }catch (IllegalArgumentException | IntrospectionException | IllegalAccessException | InvocationTargetException e){
            System.out.println("not of Type Ticket");
        }
        try {
            Hotel hotel = new ObjectMapper().convertValue(data, Hotel.class);
            property = (String) new PropertyDescriptor(getterName, Hotel.class).getReadMethod().invoke(hotel);
        }catch (IllegalArgumentException | IntrospectionException | IllegalAccessException | InvocationTargetException e){
            System.out.println("not of Type Ticket");
        }
        return property;
    }

    public String getValueByName(CPPaymentCaptureRequest request, String getterName) {
        Object data = request.getTransactionDetails().getSaleItem().getSaleDetails();
        return getValue(getterName, data);
    }

    public String getValueByName(CPPaymentRefundRequest request, String getterName) {
        Object data = request.getTransactionDetails().getSaleItem().getSaleDetails();
        return getValue(getterName, data);
    }

    public String getValueByName(CPPaymentAuthorizationRequest request, String getterName) {
        Object data = request.getTransactionDetails().getSaleItem().getSaleDetails();
        return getValue(getterName, data);
    }
}
