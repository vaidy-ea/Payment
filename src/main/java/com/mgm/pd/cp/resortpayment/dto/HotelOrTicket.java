package com.mgm.pd.cp.resortpayment.dto;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HotelOrTicket<T> implements Serializable {
    @JsonUnwrapped
    private T data;
}
