package com.mgm.pd.cp.resortpayment.dto.router;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@Builder
@AllArgsConstructor
public class RouterRequest {
    private String operation;
    private String gatewayId;
    @NotBlank(message = "requestJson is mandatory")
    private String requestJson;
}
