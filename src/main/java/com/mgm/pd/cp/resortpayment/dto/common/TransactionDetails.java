package com.mgm.pd.cp.resortpayment.dto.common;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.mgm.pd.cp.payment.common.dto.opera.TransactionAmount;
import com.mgm.pd.cp.payment.common.validation.BooleanValidator;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDetails extends BaseTransactionDetails implements Serializable {
    @JsonDeserialize(using = BooleanValidator.class)
    private Boolean isCardPresent;

    @Valid @NotNull(message = "transactionAmount cannot be empty or null")
    private TransactionAmount transactionAmount;

    @Valid
    private Customer customer;

    @Valid
    private CurrencyConversion currencyConversion;
}
