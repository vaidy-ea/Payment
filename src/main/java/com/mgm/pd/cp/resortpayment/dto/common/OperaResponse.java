package com.mgm.pd.cp.resortpayment.dto.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OperaResponse {
    private Double authAmountRequested;
    private String binRate;
    private String binCurrencyCode;
    private Double dccAmount;
    private String cardExpirationDate;
    private Long cardNumber;
    private Integer cardNumberLast4Digits;
    private String cardType;
    private Integer issueNumber;
    private String message;
    private String printInfo1;
    private String printInfo2;
    private String printInfo3;
    private String printInfo4;
    private String printInfo5;
    private String printInfo6;
    private String printInfo7;
    private String printInfo8;
    private String printInfo9;
    private String resvNameID;
    private String returnCode;
    private String sequenceNumber;
    private String startDate;
    private String transDate;
    private String uniqueID;
    private String vendorTranID;
    private String approvalCode;
    private String clientID;
    private String corelationId;
    //For UC4
    private String merchantID;
    private Double settleAmount;
    private String transReference;
}
