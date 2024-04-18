package com.mgm.pd.cp.resortpayment.exception;

public class MissingRequiredFieldException extends RuntimeException {

    public MissingRequiredFieldException(String value) {
        super(value);
    }
}
