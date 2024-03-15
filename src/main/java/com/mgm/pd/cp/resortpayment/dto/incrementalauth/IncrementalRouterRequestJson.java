package com.mgm.pd.cp.resortpayment.dto.incrementalauth;

import com.mgm.pd.cp.payment.common.constant.AuthType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class IncrementalRouterRequestJson {
    private Double authorizationAmount;
    private Double totalAuthAmount;
    private String currencyIndicator;
    private String guestName;
    private String billingAddress1;
    private String billingAddress2;
    private String billingCity;
    private String billingState;
    private String billingZIP;
    private Double dccAmount;
    private String dCCFlag;
    private String binCurrencyCode;
    private String binRate;
    private String uniqueID;
    private String cardNumber;
    private String cardExpirationDate;
    private String cardPresent;
    private String cardType;
    private String cID;
    private String trackData;
    private String trackIndicator;
    private String trackLength;
    private String startDate;
    private Integer issueNumber;
    private String usageType;
    private String chainCode;
    private String propertyCode;
    private String merchantID;
    private String version;
    private String workstation;
    private String checkOutDate;
    private String checkInDate;
    private String originDate;
    private String resvNameID;
    private String roomNum;
    private Double roomRate;
    private String vendorTranID;
    private Double balance;
    private String sequenceNumber;
    private Long originalAuthSequence;
    private String transDate;
    private AuthType authType;
    private String aVSStatus;
    private String clientID;
    private String corelationId;

    //TODO: Missing in Payload
    private String dateTime;
    private Long clerkId;
}
