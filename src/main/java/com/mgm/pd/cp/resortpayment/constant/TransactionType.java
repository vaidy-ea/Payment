package com.mgm.pd.cp.resortpayment.constant;

import lombok.Getter;

@Getter
public enum TransactionType {
    INIT_AUTH("Initial Auth"),
    INCREMENTAL_AUTH("Incremental Auth"),
    REFUND_TO_CARD("Ad-hoc Refund"),
    CAPTURE_PARTIAL_VOID("Capture Partial Void"),
    CAPTURE_ADDITIONAL_AUTH("Capture Additional Account"),
    VOID("Void");

    private final String value;

    TransactionType(String value) {
        this.value = value;
    }
}
