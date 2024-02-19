package com.mgm.pd.cp.resortpayment.dto.opera;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Card {
    private String cardType;
    private String maskedCardNumber;
    private String cardHolderName;
    private String startDate;
    private String expiryDate;
    private String cardIssuerName;
    private String cardIssuerIdentification;
    private String sequenceNumber;
    private String track1;
    private String track2;
    private String track3;
    private Boolean isTokenized;
    private String tokenType;
    private String tokenValue;
}
