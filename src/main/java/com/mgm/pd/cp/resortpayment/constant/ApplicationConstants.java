package com.mgm.pd.cp.resortpayment.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ApplicationConstants {
    public static final String SUCCESS_CODE = "200";
    public static final String SUCCESS_MESSAGE = "Success";
    public static final String SERVICE_UNAVAILABLE = "503";
    public static final String INTELLIGENT_ROUTER_CONNECTION_EXCEPTION_MESSAGE = "Can't connect to Intelligent Router Service";
    public static final String FAILURE_MESSAGE = "Unsuccessful";
    public static final String FAILURE_CODE = "422";
    public static final String VALIDATION_EXCEPTION_MESSAGE = "Validation Error";
    public static final String BAD_REQUEST = "400";
    public static final String AUTHORIZE_OPERATION = "authorize";
    public static final String CAPTURE_OPERATION = "capture";
    public static final String VOID_OPERATION = "void";
    public static final String SHIFT4_GATEWAY_ID = "shift4";
}