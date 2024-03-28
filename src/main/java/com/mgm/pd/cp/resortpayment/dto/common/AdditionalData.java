package com.mgm.pd.cp.resortpayment.dto.common;

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
public class AdditionalData implements Serializable {
    @Size(max = 50, message = "name exceeds the permissible length of 50")
    private String name;

    @Size(max = 100, message = "value exceeds the permissible length of 100")
    private String value;
}
