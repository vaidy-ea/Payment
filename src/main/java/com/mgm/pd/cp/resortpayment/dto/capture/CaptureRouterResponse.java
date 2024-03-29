package com.mgm.pd.cp.resortpayment.dto.capture;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mgm.pd.cp.payment.common.constant.IssuerType;
import com.mgm.pd.cp.payment.common.validation.ValidDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CaptureRouterResponse {
    @ValidDate(format = "MMyy", message = "invalid CardExpirationDate, expected format is MMyy")
    @NotBlank(message = "CardExpirationDate can't be empty or NULL")
    @Size(min = 4, max = 4, message = "please enter CardExpirationDate in MMyy format")
    private String cardExpirationDate;
    @NotBlank(message = "CardNumber can't be empty or NULL")
    @Size(max = 30, message = "CardNumber exceed the permissible length")
    private String cardNumber;
    @Enumerated(EnumType.STRING)
    private IssuerType cardType;
    @Size(max = 20, message = "MerchantID exceed the permissible length")
    private String merchantID;
    @NotBlank(message = "ResvNameID can't be empty or NULL")
    @Size(max = 20, message = "ResvNameID exceed the permissible length")
    private String resvNameID;
    private String returnCode;
    @NotBlank(message = "SequenceNumber can't be empty or NULL")
    @Size(max = 10, message = "SequenceNumber exceed the permissible length")
    private String sequenceNumber;
    @ValidDate(format = "yyyyMMddhh:mm:ss", message = "invalid TransDate, expected format is YYYYMMDDHH:MM:SS")
    @NotBlank(message = "TransDate can't be empty or NULL")
    @Size(min = 16, max = 16, message = "please enter TransDate in YYYYMMDDHH:MM:SS format")
    private String transDate;
    @Size(max = 30, message = "UniqueID exceed the permissible length")
    private String uniqueID;
    @JsonProperty("vendorTranId")
    @Size(max = 20, message = "VendorTranID exceed the permissible length")
    private String vendorTranID;
    @NotBlank(message = "Client ID can't be empty or NULL")
    private String clientID;
    @NotBlank(message = "Corelation ID can't be empty or NULL")
    private String corelationId;
    private String dateTime;
    private Double totalAuthAmount;
    private String approvalCode;
}
