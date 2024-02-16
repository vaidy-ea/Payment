package com.mgm.pd.cp.resortpayment.constant;

import lombok.Getter;

@Getter
public enum ReturnCode {
    APPROVAL("Approval"),
    REFERRAL("Call bank for Authorization"),
    PICKUP("Keep card"),
    REENTER("Resend"),
    DECLINE("Not Approved"),
    MISC("Miscellaneous"),
    ACCEPTED("Accepted"),
    FAILED("Rejected");

    private final String value;

    ReturnCode(String returnCode) {
        this.value = returnCode;
    }
}
