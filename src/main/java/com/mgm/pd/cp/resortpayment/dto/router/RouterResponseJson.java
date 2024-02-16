package com.mgm.pd.cp.resortpayment.dto.router;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RouterResponseJson implements Serializable {
    private String responseJson;
}
