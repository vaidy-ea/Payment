package com.mgm.pd.cp.resortpayment.exception;

import java.util.List;

public class MissingHeaderException extends RuntimeException{
    public MissingHeaderException(List<String> value){
        super(value.toString());
    }
}
