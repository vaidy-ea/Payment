package com.mgm.pd.cp.resortpayment.dto.authorize;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthorizationRouterResponse {
    private String dateTime;
    private Double totalAuthAmount;
    private String binRate;
    private String binCurrencyCode;
    private Long dCCAmount;
    private String cardExpirationDate;
    private Integer cardNumber;
    private String cardType;
    private Integer issueNumber;
    @JsonProperty("resvNameId")
    private String resvNameID;
    private String sequenceNumber;
    private String startDate;
    private String transDate;
    private String uniqueID;
    @JsonProperty("vendorTranId")
    private String vendorTranID;
    private String clientID;
    private String corelationId;

    //TODO: Needs mapping for below
    private Double authAmountRequested;
    private Integer cardNumberLast4Digits;
    private String returnCode;
    private String message;
    private String approvalCode;
    private String printInfo1;
    private String printInfo2;
    private String printInfo3;
    private String printInfo4;
    private String printInfo5;
    private String printInfo6;
    private String printInfo7;
    private String printInfo8;
    private String printInfo9;
    private String comments;
}
