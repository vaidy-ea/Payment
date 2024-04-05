package com.mgm.pd.cp.resortpayment.dto.common;

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
public class Ticket implements Serializable {
    @Size(max = 50, message = "eventIdentifier exceeds the permissible length of 50")
    private String eventIdentifier;

    @Size(max = 30, message = "ticketNumber exceeds the permissible length of 30")
    private String ticketNumber;

    @Size(max = 10, message = "ticketRate exceeds the permissible length of 10")
    private String ticketRate;

    @PossibleDateTime(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX", fallbackPatterns = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", message = "invalid showDate, expected format is yyyy-MM-ddThh:mm:ssZ")
    private String showDate;
}
