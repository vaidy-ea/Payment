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
public class Merchant implements Serializable {
    @Size(max = 50, message = "merchantIdentifier exceeds the permissible length of 50")
    private String merchantIdentifier;

    @Size(max = 100, message = "terminalIdentifier exceeds the permissible length of 100")
    private String terminalIdentifier;

    @Size(max = 30, message = "version exceeds the permissible length of 30")
    private String version;

    @Size(max = 50, message = "clerkIdentifier exceeds the permissible length of 50")
    private String clerkIdentifier;
}
