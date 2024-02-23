package com.mgm.pd.cp.resortpayment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Address implements Serializable {
    private String addressLine;
    private String streetName;
    private String buildingNumber;
    private String postCode;
    private String townName;
    private String countrySubDivision;
    private String country;
}
