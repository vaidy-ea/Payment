package com.mgm.pd.cp.resortpayment.dto;

import com.mgm.pd.cp.payment.common.dto.opera.GatewayInfo;
import com.mgm.pd.cp.payment.common.validation.possibledatetime.PossibleDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    @PossibleDateTime(pattern = "yyyy-MM-dd", fallbackPatterns = "yyyy-MM-dd'T'HH:mm:ss'Z'", message = "invalid transactionDateTime, expected format is yyyy-MM-ddThh:mm:ssZ")
    @NotBlank(message = "transactionDateTime can't be empty or NULL")
    private String transactionDateTime;

    @Size(max = 40, message = "transactionLOB exceed the permissible length")
    private String transactionLOB;

    private GatewayInfo gatewayInfo;

    //TODO: Missing in Payload sheet
    private String clientID;
    private String corelationId;
    private Long incrementalAuthInvoiceId;
    private String referenceId;
    private String dateTime;
    private Long clerkId;
    private String comments;
}
