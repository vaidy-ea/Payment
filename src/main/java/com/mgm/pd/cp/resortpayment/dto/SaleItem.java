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
public class SaleItem<T> implements Serializable {
    private String saleType;
    private String saleDate;
    private String saleReferenceIdentifier;
    private T saleDetails;
}
