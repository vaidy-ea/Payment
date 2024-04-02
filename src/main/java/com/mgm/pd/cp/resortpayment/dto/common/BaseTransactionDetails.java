package com.mgm.pd.cp.resortpayment.dto.common;

import com.mgm.pd.cp.payment.common.dto.opera.Card;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BaseTransactionDetails {
    @Valid @NotNull(message = "card details can't be empty or null")
    private Card card;

    @Valid
    private SaleItem saleItem;

    @Valid
    private List<AdditionalData> additionalData;

    private String cardEntryMode;
}
