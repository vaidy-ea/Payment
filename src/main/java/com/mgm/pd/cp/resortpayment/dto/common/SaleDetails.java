package com.mgm.pd.cp.resortpayment.dto.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.Getter;

import javax.validation.Valid;
import java.io.Serializable;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SaleDetails implements Serializable {
    @JsonUnwrapped @Valid
    private Hotel hotel;

    @JsonUnwrapped @Valid
    private Ticket ticket;
}
