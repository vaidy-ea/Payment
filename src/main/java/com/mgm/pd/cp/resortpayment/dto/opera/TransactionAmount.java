package com.mgm.pd.cp.resortpayment.dto.opera;

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
public class TransactionAmount {
    private Double balanceAmount;
    private Double requestedAmount;
    private Double authorizedAmount;
    private Double cumulativeAmount;
    private String currencyIndicator;
    private DetailedAmount detailedAmount;
}
