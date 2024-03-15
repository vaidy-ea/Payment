package com.mgm.pd.cp.resortpayment.dto.common;

import com.mgm.pd.cp.payment.common.validation.possibledatetime.PossibleDateTime;
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
public class SaleItem<T> implements Serializable {
    @Size(max = 70, message = "saleType exceeds the permissible length of 70")
    private String saleType;

    @PossibleDateTime(pattern = "yyyy-MM-dd", fallbackPatterns = "yyyy-MM-dd'T'HH:mm:ss'Z'", message = "invalid saleDate, expected format is yyyy-MM-ddThh:mm:ssZ")
    private String saleDate;

    @Size(max = 70, message = "saleReferenceIdentifier exceeds the permissible length of 70")
    private String saleReferenceIdentifier;

    @Valid
    private T saleDetails;
}
