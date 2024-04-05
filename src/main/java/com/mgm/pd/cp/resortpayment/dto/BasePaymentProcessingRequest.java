package com.mgm.pd.cp.resortpayment.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.mgm.pd.cp.payment.common.constant.TransactionLOB;
import com.mgm.pd.cp.payment.common.dto.CPRequestHeaders;
import com.mgm.pd.cp.payment.common.validation.TransactionLOBDeserializer;
import com.mgm.pd.cp.payment.common.validation.possibledatetime.PossibleDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BasePaymentProcessingRequest {
    @NotBlank(message = "transactionIdentifier can't be empty or NULL")
    @Size(max = 40, message = "transactionIdentifier exceed the permissible length")
    private String transactionIdentifier;

    @Size(max = 40, message = "originalTransactionIdentifier exceed the permissible length")
    private String originalTransactionIdentifier;

    @PossibleDateTime(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX", fallbackPatterns = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", message = "invalid transactionDateTime, expected format is yyyy-MM-ddThh:mm:ssZ")
    @NotBlank(message = "transactionDateTime can't be empty or NULL")
    private String transactionDateTime;

    @Valid @Enumerated(EnumType.STRING)
    @JsonDeserialize(using = TransactionLOBDeserializer.class)
    private TransactionLOB transactionLOB;

    //TODO: Missing in Payload sheet
    private String referenceId;
    @Valid
    private CPRequestHeaders headers;
}
