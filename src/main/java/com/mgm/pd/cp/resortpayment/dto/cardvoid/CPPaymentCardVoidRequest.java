package com.mgm.pd.cp.resortpayment.dto.cardvoid;

import com.mgm.pd.cp.resortpayment.dto.BasePaymentProcessingRequest;
import com.mgm.pd.cp.resortpayment.dto.BaseTransactionDetails;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@EqualsAndHashCode(callSuper = true)
@Data
public class CPPaymentCardVoidRequest extends BasePaymentProcessingRequest {
    @Valid @NotNull(message = "transactionDetails can't be empty or null")
    private BaseTransactionDetails transactionDetails;
}
