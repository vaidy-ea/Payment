package com.mgm.pd.cp.resortpayment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CPPaymentProcessingRequest extends BasePaymentProcessingRequest {
    @Size(max = 40, message = "transactionType exceed the permissible length")
    private String transactionType;

    @Valid @NotNull(message = "transactionDetails can't be empty or null")
    private TransactionDetails transactionDetails;
}
