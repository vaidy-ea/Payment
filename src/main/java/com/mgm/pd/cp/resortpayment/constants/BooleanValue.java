package com.mgm.pd.cp.resortpayment.constants;

import lombok.Getter;

@Getter
public enum BooleanValue {
    Y("true"),
    N("false");

    private final String value;

    BooleanValue(String bool) {
        this.value = bool;
    }

    public static String getEnumByString(String code){
        for(BooleanValue e : BooleanValue.values()){
            if(e.getValue().equals(code)) return e.name();
        }
        return null;
    }
}
