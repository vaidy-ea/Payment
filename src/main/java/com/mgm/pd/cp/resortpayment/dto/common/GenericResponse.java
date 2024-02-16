package com.mgm.pd.cp.resortpayment.dto.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GenericResponse<T> {
    private String message;
    private String code;
    private T data;
}
