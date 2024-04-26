package com.mgm.pd.cp.resortpayment.exception;

public class InvalidTransactionAttemptException extends RuntimeException {
    public InvalidTransactionAttemptException(String value) {
        super(value);
    }
}
