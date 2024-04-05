package com.mgm.pd.cp.resortpayment.dto.cardvoid;

import com.mgm.pd.cp.resortpayment.dto.BasePaymentProcessingRequest;
import com.mgm.pd.cp.resortpayment.dto.common.BaseTransactionDetails;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@EqualsAndHashCode(callSuper = true)
@Data
public class CPPaymentCardVoidRequest extends BasePaymentProcessingRequest {
    @NotNull(message = "transactionAuthChainId can't be empty or NULL")
    private String transactionAuthChainId;

    @Valid
    private BaseTransactionDetails transactionDetails;
}
