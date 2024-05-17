package com.mgm.pd.cp.resortpayment.dto.authorize;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mgm.pd.cp.payment.common.constant.IssuerType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

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
    @Enumerated(EnumType.STRING)
    private IssuerType cardType;
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
    private Double authAmountRequested;
    private Integer cardNumberLast4Digits;
    private String returnCode;
    private String approvalCode;
    private String message;
    private String avsResult;
    private String gatewayID;
    private String responseReason;
    private String reasonDescription;
}
