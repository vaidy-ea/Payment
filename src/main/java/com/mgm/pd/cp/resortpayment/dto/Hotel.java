package com.mgm.pd.cp.resortpayment.dto;

import com.mgm.pd.cp.payment.common.validation.possibledatetime.PossibleDateTime;
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
public class Hotel implements Serializable {
    @Size(max = 50, message = "propertyIdentifier exceeds the permissible length of 50")
    private String propertyIdentifier;

    @Size(max = 50, message = "propertyChainIdentifier exceeds the permissible length of 50")
    private String propertyChainIdentifier;

    @Size(max = 30, message = "roomNumber exceeds the permissible length of 30")
    private String roomNumber;

    @Size(max = 10, message = "roomRate exceeds the permissible length of 10")
    private String roomRate;

    @PossibleDateTime(pattern = "yyyy-MM-dd", fallbackPatterns = "yyyy-MM-dd'T'HH:mm:ss'Z'", message = "invalid originDate, expected format is yyyy-MM-ddThh:mm:ssZ")
    private String originDate;

    @PossibleDateTime(pattern = "yyyy-MM-dd", fallbackPatterns = "yyyy-MM-dd'T'HH:mm:ss'Z'", message = "invalid departureDate, expected format is yyyy-MM-ddThh:mm:ssZ")
    private String departureDate;

    @PossibleDateTime(pattern = "yyyy-MM-dd", fallbackPatterns = "yyyy-MM-dd'T'HH:mm:ss'Z'", message = "invalid checkInDate, expected format is yyyy-MM-ddThh:mm:ssZ")
    @Size(max = 70, message = "checkInDate exceeds the permissible length of 70")
    private String checkInDate;

    @PossibleDateTime(pattern = "yyyy-MM-dd", fallbackPatterns = "yyyy-MM-dd'T'HH:mm:ss'Z'", message = "invalid checkOutDate, expected format is yyyy-MM-ddThh:mm:ssZ")
    @Size(max = 70, message = "checkOutDate exceeds the permissible length of 70")
    private String checkOutDate;

    private Integer estimatedDuration;
}
