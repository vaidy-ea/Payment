package com.mgm.pd.cp.resortpayment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Hotel extends BasePropertyIdentifier implements Serializable {
    private String propertyChainIdentifier;
    private String roomNumber;
    private String roomRate;
    private String originDate;
    private String departureDate;
    private String checkInDate;
    private String checkOutDate;
    private Integer estimatedDuration;
}
