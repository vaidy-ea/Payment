package com.mgm.pd.cp.resortpayment.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.mgm.pd.cp.payment.common.constant.AuthType;
import com.mgm.pd.cp.payment.common.constant.TransactionLOB;
import com.mgm.pd.cp.payment.common.dto.CPRequestHeaders;
import com.mgm.pd.cp.payment.common.validation.deserializer.TransactionLOBDeserializer;
import com.mgm.pd.cp.payment.common.validation.possibledatetime.PossibleDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import static com.mgm.pd.cp.payment.common.constant.ApplicationConstants.ALPHA_NUMERIC_AND_SPACE;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BasePaymentProcessingRequest {
    @NotBlank(message = "transactionIdentifier can't be empty or NULL")
    @Size(max = 40, message = "transactionIdentifier exceed the permissible length")
    @Pattern(regexp = ALPHA_NUMERIC_AND_SPACE, message = "transactionIdentifier is allowed to have only alpha numeric or space")
    private String transactionIdentifier;

    @Size(max = 40, message = "originalTransactionIdentifier exceed the permissible length")
    private String originalTransactionIdentifier;

    @PossibleDateTime(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX", fallbackPatterns = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", message = "invalid transactionDateTime, expected format is yyyy-MM-ddThh:mm:ssZ")
    @NotBlank(message = "transactionDateTime can't be empty or NULL")
    private String transactionDateTime;

    @Valid @Enumerated(EnumType.STRING)
    @JsonDeserialize(using = TransactionLOBDeserializer.class)
    private TransactionLOB transactionLOB;

    @Valid @Enumerated(EnumType.STRING)
    private AuthType transactionType;

    private String referenceId;

    @Valid
    private CPRequestHeaders headers;
}
