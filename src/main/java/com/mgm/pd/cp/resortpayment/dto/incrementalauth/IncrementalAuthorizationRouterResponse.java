package com.mgm.pd.cp.resortpayment.dto.incrementalauth;

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
public class IncrementalAuthorizationRouterResponse {
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
    private String approvalCode;
}
