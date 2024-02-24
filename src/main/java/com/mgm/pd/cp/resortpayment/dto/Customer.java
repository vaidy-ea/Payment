package com.mgm.pd.cp.resortpayment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.Size;
import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Customer implements Serializable {
    @Size(max = 70, message = "fullName exceeds the permissible length of 70")
    private String fullName;

    @Size(max = 70, message = "firstName exceeds the permissible length of 70")
    private String firstName;

    @Size(max = 70, message = "lastName exceeds the permissible length of 70")
    private String lastName;

    @Valid
    private Address billingAddress;
}
