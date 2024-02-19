package com.mgm.pd.cp.resortpayment.dto.cardvoid;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mgm.pd.cp.resortpayment.constant.CardType;
import com.mgm.pd.cp.resortpayment.validation.ValidDate;
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
public class CardVoidRouterResponse {
    @ValidDate(format = "MMyy", message = "invalid CardExpirationDate, expected format is MMyy")
    @NotBlank(message = "CardExpirationDate can't be empty or NULL")
    @Size(min = 4, max = 4, message = "please enter CardExpirationDate in MMyy format")
    private String cardExpirationDate;
    @NotBlank(message = "CardNumber can't be empty or NULL")
    @Size(max = 30, message = "CardNumber exceed the permissible length")
    private String cardNumber;
    @Enumerated(EnumType.STRING)
    private CardType cardType;
    @Size(max = 20, message = "MerchantID exceed the permissible length")
    private String merchantID;
    private Double settleAmount;
    private String message;
    private String printInfo1;
    private String printInfo2;
    private String printInfo3;
    private String printInfo4;
    private String printInfo5;
    private String printInfo6;
    private String printInfo7;
    private String printInfo8;
    private String printInfo9;
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
    private String transReference;
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
    private String comments;
}
