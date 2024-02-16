package com.mgm.pd.cp.resortpayment.constant;

import lombok.Getter;

@Getter
public enum AuthType {

    INIT("Initial Authorization"),
    SUPP("Supplemental Authorization"),
    CREDIT("Authorization Reversal"),
    AR("Accounts Receivable"),
    DEPOSIT("Advanced Deposit");

    private final String value;

    AuthType(String authType) {
        this.value = authType;
    }
}
