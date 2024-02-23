package com.mgm.pd.cp.resortpayment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CurrencyConversion implements Serializable {
    private String amount;
    private String conversionIdentifier;
    private String conversionFlag;
    private String binCurrencyCode;
    private String binCurrencyRate;
}
