package com.mgm.pd.cp.resortpayment.exception;

public class InvalidTransactionTypeException extends RuntimeException {
    public InvalidTransactionTypeException(String value) {
        super(value);
    }
}
