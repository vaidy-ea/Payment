package com.mgm.pd.cp.resortpayment.dto.common;

import com.mgm.pd.cp.payment.common.constant.BooleanValue;
import com.mgm.pd.cp.payment.common.validation.currencyvalidation.ValidCurrencyCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.Valid;
import javax.validation.constraints.Size;
import java.io.Serializable;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CurrencyConversion implements Serializable {

    private Double amount;

    @Size(max = 20, message = "conversionIdentifier exceeds the permissible length of 20")
    private String conversionIdentifier;

    @Valid @Enumerated(EnumType.STRING)
    private BooleanValue conversionFlag;

    @ValidCurrencyCode(message = "Invalid binCurrencyCode", optional = true)
    @Size(max = 3, message = "binCurrencyCode exceeds the permissible length of 3")
    private String binCurrencyCode;

    @Size(max = 14, message = "binCurrencyRate exceeds the permissible length of 14")
    private String binCurrencyRate;
}
