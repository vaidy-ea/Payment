package com.mgm.pd.cp.resortpayment.dto;

import com.mgm.pd.cp.payment.common.dto.opera.Card;
import com.mgm.pd.cp.payment.common.dto.opera.TransactionAmount;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDetails implements Serializable {
    @NotNull(message = "isCardPresent cannot be empty or null")
    private Boolean isCardPresent;

    @Valid @NotNull(message = "transactionAmount cannot be empty or null")
    private TransactionAmount transactionAmount;

    @Valid @NotNull(message = "card details can't be empty or null")
    private Card card;

    @Valid
    private Customer customer;

    @Valid
    private CurrencyConversion currencyConversion;

    @Valid @NotNull(message = "merchant details can't be empty or null")
    private Merchant merchant;

    @Valid
    private SaleItem saleItem;

    @Valid
    private List<AdditionalData> additionalData;
}
