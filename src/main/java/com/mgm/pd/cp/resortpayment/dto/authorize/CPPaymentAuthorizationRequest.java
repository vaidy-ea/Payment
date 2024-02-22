package com.mgm.pd.cp.resortpayment.dto.authorize;

import com.mgm.pd.cp.payment.common.constant.AuthType;
import com.mgm.pd.cp.payment.common.constant.CardType;
import com.mgm.pd.cp.payment.common.validation.ValidDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.*;
import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CPPaymentAuthorizationRequest implements Serializable {
    @NotNull(message = "AuthorizationAmount can't be empty or NULL")
    @Min(value = 0L, message = "AuthorizationAmount can't be negative")
    @Max(value = 99999999999999999L)
    private Double authorizationAmount;
    @NotNull(message = "TotalAuthAmount can't be empty or NULL")
    @Min(value = 0L, message = "TotalAuthAmount can't be negative")
    @Max(value = 99999999999999999L, message = "TotalAuthAmount can't be greater than 99999999999999999 ")
    private Double totalAuthAmount;
    @NotBlank(message = "CurrencyIndicator can't be empty or NULL")
    @Size(max = 3, message = "CurrencyIndicator exceed the permissible length")
    private String currencyIndicator;
    @NotBlank(message = "guestName can't be empty or NULL")
    @Size(max = 80, message = "guestName exceed the permissible length")
    private String guestName;
    @Size(max = 45, message = "BillingAddress1 exceed the permissible length")
    private String billingAddress1;
    @Size(max = 45, message = "BillingAddress2 exceed the permissible length")
    private String billingAddress2;
    @Size(max = 45, message = "BillingCity exceed the permissible length")
    private String billingCity;
    @Size(max = 2, message = "BillingState exceed the permissible length")
    private String billingState;
    @Size(max = 9, message = "BillingZIP exceed the permissible length")
    private String billingZIP;
    @Min(value = 0L, message = "DCCAmount can't be negative")
    @Max(value = 99999999999999999L)
    private Double dCCAmount;
    @Size(min = 1, max = 1, message = "DCCFlag exceed the permissible length")
    private String dCCFlag;
    @NotBlank(message = "BinCurrencyCode can't be empty or NULL")
    @Size(max = 3, message = "BinCurrencyCode exceed the permissible length")
    private String binCurrencyCode;
    @NotBlank(message = "BinRate can't be empty or NULL")
    @Size(max = 14, message = "BinRate exceed the permissible length")
    private String binRate;
    @Size(max = 30, message = "UniqueID exceed the permissible length")
    private String uniqueID;
    @ValidDate(format = "MMyy", message = "invalid CardExpirationDate, expected format is MMyy")
    @NotBlank(message = "CardExpirationDate can't be empty or NULL")
    @Size(min = 4, max = 4, message = "please enter CardExpirationDate in MMyy format")
    private String cardExpirationDate;
    @NotBlank(message = "CardNumber can't be empty or NULL")
    @Size(max = 30, message = "CardNumber exceed the permissible length")
    private String cardNumber;
    @Size(max = 1, message = "CardPresent exceed the permissible length")
    private String cardPresent;
    @Enumerated(EnumType.STRING)
    private CardType cardType;
    @Size(max = 6, message = "CID exceed the permissible length")
    private String cid;
    @Size(max = 79, message = "TrackData exceed the permissible length")
    private String trackData;
    @NotBlank(message = "TrackIndicator can't be empty or NULL")
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
    @NotBlank(message = "ChainCode/Hotel Chain Id can't be empty or NULL")
    @Size(max = 40, message = "ChainCode/Hotel Chain Id exceed the permissible length")
    private String chainCode;
    @NotBlank(message = "PropertyCode/Unique Code can't be empty or NULL")
    @Size(max = 20, message = "PropertyCode/Unique Code exceed the permissible length")
    private String propertyCode;
    @Size(max = 20, message = "MerchantID exceed the permissible length")
    private String merchantID;
    @NotBlank(message = "Version can't be empty or NULL")
    @Size(max = 10, message = "Version exceed the permissible length")
    private String version;
    @NotBlank(message = "Workstation/PMS Terminal Id can't be empty or NULL")
    @Size(max = 100, message = "Workstation/PMS Terminal Id exceed the permissible length")
    private String workstation;
    @ValidDate(message = "invalid checkOutDate, expected format is YYYYMMDD")
    @Size(min = 8, max = 8, message = "please enter checkOutDate in YYYYMMDD format")
    private String checkOutDate;
    @ValidDate(message = "invalid originDate, expected format is YYYYMMDD")
    @Size(min = 8, max = 8, message = "please enter originDate in YYYYMMDD format")
    private String originDate;
    @ValidDate(message = "invalid checkInDate, expected format is YYYYMMDD")
    @Size(min = 8, max = 8, message = "please enter checkInDate in YYYYMMDD format")
    private String checkInDate;
    @NotBlank(message = "ResvNameID can't be empty or NULL")
    @Size(max = 20, message = "ResvNameID exceed the permissible length")
    private String resvNameID;
    @Size(max = 8, message = "RoomNum exceed the permissible length")
    private String roomNum;
    @Min(value = 0L, message = "RoomRate can't be negative")
    @Max(value = 99999999999999999L, message = "RoomRate exceed the permissible length")
    private Double roomRate;
    @NotNull(message = "Balance can't be empty or NULL")
    @Min(value = 0L, message = "Balance can't be negative")
    @Max(value = 99999999999999999L, message = "Balance exceed the permissible length")
    private Double balance;
    @Size(max = 20, message = "VendorTranID exceed the permissible length")
    private String vendorTranID;
    @NotBlank(message = "SequenceNumber can't be empty or NULL")
    @Size(max = 10, message = "SequenceNumber exceed the permissible length")
    private String sequenceNumber;
    @NotNull(message = "OriginalAuthSequence can't be empty or NULL")
    @Digits(integer = 10, fraction = 0, message = "OriginalAuthSequence exceed the permissible length")
    @Min(value = 0L, message = "OriginalAuthSequence can't be negative")
    private Long originalAuthSequence;
    @ValidDate(format = "yyyyMMddhh:mm:ss", message = "invalid TransDate, expected format is YYYYMMDDHH:MM:SS")
    @NotBlank(message = "TransDate can't be empty or NULL")
    @Size(min = 16, max = 16, message = "please enter TransDate in YYYYMMDDHH:MM:SS format")
    private String transDate;
    @NotNull(message = "AuthType can't be empty or NULL")
    @Enumerated(EnumType.STRING)
    private AuthType authType;
    @Size(max = 1, message = "AVSStatus exceed the permissible length")
    private String aVSStatus;
    private String clientID;
    private String corelationId;

    //TODO: Missing in Payload sheet
    private Long incrementalAuthInvoiceId;
    private String dateTime;
    private Long clerkId;
}
