package com.mgm.pd.cp.resortpayment.dto.refund;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.mgm.pd.cp.payment.common.validation.validdate.ValidDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RefundRouterRequestJson {
    @Min(value = 0L, message = "Amount can't be negative")
    @Max(value = 99999999999999999L)
    private Double amount;
    @Min(value = 0L, message = "Amount can't be negative")
    @Max(value = 9999999999L)
    private Double taxAmount;
    @Min(value = 0L, message = "TotalAuthAmount can't be negative")
    @Max(value = 99999999999999999L)
    private Double totalAuthAmount;
    @Size(max = 80, message = "guestName exceed the permissible length")
    private String guestName;
    @Digits(integer = 17, fraction = 0, message = "DCCAmount exceed the permissible length")
    @Min(value = 0L, message = "DCCAmount can't be negative")
    @Max(value = 99999999999999999L)
    private Double dccAmount;
    @Digits(integer = 14, fraction = 0, message = "DCCAmount exceed the permissible length")
    @Min(value = 0L, message = "DCCAmount can't be negative")
    @Max(value = 9999999999999L)
    private Double dccControlNumber;
    @Size(min = 1, max = 1, message = "DCCFlag exceed the permissible length")
    private String dCCFlag;
    @Size(max = 3, message = "BinCurrencyCode exceed the permissible length")
    private String binCurrencyCode;
    @Size(max = 14, message = "BinRate exceed the permissible length")
    private String binRate;
    @Size(max = 30, message = "UniqueID exceed the permissible length")
    private String uniqueID;
    @ValidDate(format = "MMyy", message = "invalid CardExpirationDate, expected format is MMyy")
    @Size(min = 4, max = 4, message = "please enter CardExpirationDate in MMyy format")
    private String cardExpirationDate;
    @Size(max = 30, message = "CardNumber exceed the permissible length")
    private String cardNumber;
    @Size(max = 1, message = "CardPresent exceed the permissible length")
    private String cardPresent;
    private String currencyIndicator;
    private String cardType;
    @Size(max = 79, message = "TrackData exceed the permissible length")
    private String trackData;
    @Size(min = 1, max = 1, message = "TrackIndicator exceed the permissible length")
    private String trackIndicator;
    @Size(max = 2, message = "TrackLength exceed the permissible length")
    private String trackLength;
    @ValidDate(format = "yyMM", message = "invalid startDate, expected format is yyMM")
    @Size(min = 4, max = 4, message = "please enter startDate in yyMM format")
    private String startDate;
    @NotBlank(message = "UsageType can't be empty or NULL")
    @Size(min = 1, max = 1, message = "UsageType exceed the permissible length")
    private String usageType;
    @Digits(integer = 2, fraction = 0, message = "IssueNumber exceed the permissible length")
    private Integer issueNumber;
    @Size(max = 40, message = "ChainCode/Hotel Chain Id exceed the permissible length")
    private String chainCode;
    @Size(max = 20, message = "PropertyCode/Unique Code exceed the permissible length")
    private String propertyCode;
    @Size(max = 20, message = "MerchantID exceed the permissible length")
    private String merchantID;
    @Size(max = 10, message = "Version exceed the permissible length")
    private String version;
    @Size(max = 100, message = "Workstation/PMS Terminal Id exceed the permissible length")
    private String workstation;
    @Size(min = 1, max = 1, message = "MessageResend exceed the permissible length")
    private String messageResend;
    @ValidDate(format = "MMddyy", message = "invalid departureDate, expected format is MMddyy")
    @Size(min = 6, max = 6, message = "please enter departureDate in MMddyy format")
    private String departureDate;
    @Size(max = 20, message = "ResvNameID exceed the permissible length")
    private String resvNameID;
    @Size(max = 8, message = "RoomNum exceed the permissible length")
    private String roomNum;
    @Min(value = 0L, message = "RoomRate can't be negative")
    @Max(value = 99999999999999999L, message = "RoomRate exceed the permissible length")
    private Double roomRate;
    @ValidDate(format = "MMddyy", message = "invalid arrivalDate, expected format is MMDDYY")
    @Size(min = 6, max = 6, message = "please enter arrivalDate in MMDDYY format")
    private String arrivalDate;
    @Size(max = 20, message = "VendorTranID exceed the permissible length")
    private String vendorTranID;
    @Size(max = 10, message = "SequenceNumber exceed the permissible length")
    private String sequenceNumber;
    @Digits(integer = 10, fraction = 0, message = "OriginalAuthSequence exceed the permissible length")
    @Min(value = 0L, message = "OriginalAuthSequence can't be negative")
    private Long originalAuthSequence;
    @ValidDate(format = "yyyyMMddhh:mm:ss", message = "invalid TransDate, expected format is YYYYMMDDHH:MM:SS")
    @Size(min = 16, max = 16, message = "please enter TransDate in YYYYMMDDHH:MM:SS format")
    private String transDate;
    @Size(max = 6, message = "ApprovalCode exceed the permissible length")
    private String approvalCode;
    @Size(max = 10, message = "MessageType exceed the permissible length")
    private String messageType;
    @Digits(integer = 2, fraction = 0, message = "Installments exceed the permissible length")
    private Integer installments;
    private String clientID;
    private String corelationId;
    private Long clerkId;
    private String billingZIP;
}
