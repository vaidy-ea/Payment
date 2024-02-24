package com.mgm.pd.cp.resortpayment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Size;
import java.io.Serializable;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CurrencyConversion implements Serializable {
    @Size(max = 70, message = "amount exceeds the permissible length of 70")
    private String amount;

    @Size(max = 70, message = "conversionIdentifier exceeds the permissible length of 70")
    private String conversionIdentifier;

    @Size(max = 70, message = "conversionFlag exceeds the permissible length of 70")
    private String conversionFlag;

    @Size(max = 70, message = "binCurrencyCode exceeds the permissible length of 70")
    private String binCurrencyCode;

    @Size(max = 70, message = "binCurrencyRate exceeds the permissible length of 70")
    private String binCurrencyRate;
}
