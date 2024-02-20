package com.mgm.pd.cp.resortpayment.constant;

import lombok.Getter;

@Getter
public enum TransactionType {
    INIT_AUTH("Initial Auth"),
    INIT_AUTH_CNP("Initial Auth Card Not Present"),
    INCREMENTAL_AUTH("Incremental Auth"),
    REFUND_TO_CARD("Ad-hoc Refund"),
    CAPTURE_PARTIAL_VOID("Capture Partial Void"),
    CAPTURE_ADDITIONAL_AUTH("Capture Additional Account"),
    CARD_VOID("Card Void");

    private final String value;

    TransactionType(String value) {
        this.value = value;
    }
}
