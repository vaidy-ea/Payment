package com.mgm.pd.cp.resortpayment.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ApplicationConstants {
    public static final String SUCCESS_MESSAGE = "Success";
    public static final String INTELLIGENT_ROUTER_CONNECTION_EXCEPTION_MESSAGE = "Can't connect to Intelligent Router Service";
    public static final String AUTHORIZE_OPERATION = "authorize";
    public static final String CAPTURE_OPERATION = "capture";
    public static final String VOID_OPERATION = "void";
    public static final String SHIFT4_GATEWAY_ID = "shift4";
    public static final String INITIAL_PAYMENT_IS_MISSING = "Initial Payment is missing";
}