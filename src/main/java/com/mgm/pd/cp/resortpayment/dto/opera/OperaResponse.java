package com.mgm.pd.cp.resortpayment.dto.opera;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OperaResponse {
    private String approvalCode;
    private String responseCode;
    private String responseReason;
    private GatewayInfo gatewayInfo;
    private String networkIdentifier;
    private String originalTransactionIdentifier;
    private String transactionDateTime;
    private TransactionAmount transactionAmount;
    private Card card;
    private List<PrintDetails> printDetails;
}
