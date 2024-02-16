package com.mgm.pd.cp.resortpayment.dto.exception;

import lombok.Data;

@Data
public class IntelligentRouterException {
    private String status;
    private String origin;
    private int code;
    private String message;
    private String shortMessage;
}
