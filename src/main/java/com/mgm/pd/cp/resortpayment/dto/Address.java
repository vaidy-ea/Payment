package com.mgm.pd.cp.resortpayment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Size;
import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Address implements Serializable {
    @Size(max = 70, message = "addressLine exceeds the permissible length of 70")
    private String addressLine;

    @Size(max = 70, message = "streetName exceeds the permissible length of 70")
    private String streetName;

    @Size(max = 16, message = "buildingNumber exceeds the permissible length of 16")
    private String buildingNumber;

    @Size(max = 16, message = "postCode exceeds the permissible length of 16")
    private String postCode;

    @Size(max = 35, message = "townName exceeds the permissible length of 35")
    private String townName;

    @Size(max = 35, message = "countrySubDivision exceeds the permissible length of 35")
    private String countrySubDivision;

    @Size(max = 35, message = "country exceeds the permissible length of 35")
    private String country;
}
