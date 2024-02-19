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
public class DetailedAmount {
    private Double amount;
    private Double cashBack;
    private Double gratuity;
    private Double fees;
    private Double rebate;
    private Double vat;
    private Double surcharge;
}
