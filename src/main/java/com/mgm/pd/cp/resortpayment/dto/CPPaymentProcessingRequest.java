package com.mgm.pd.cp.resortpayment.dto;

import com.mgm.pd.cp.payment.common.constant.AuthType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CPPaymentProcessingRequest extends BasePaymentProcessingRequest {

    @Valid @Enumerated(EnumType.STRING)
    private AuthType transactionType;

    @Valid @NotNull(message = "transactionDetails can't be empty or null")
    private TransactionDetails transactionDetails;
}
