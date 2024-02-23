package com.mgm.pd.cp.resortpayment.dto;

import com.mgm.pd.cp.payment.common.dto.opera.Card;
import com.mgm.pd.cp.payment.common.dto.opera.TransactionAmount;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDetails implements Serializable {
    private Boolean isCardPresent;
    private TransactionAmount transactionAmount;
    private Card card;
    private Customer customer;
    private CurrencyConversion currencyConversion;
    private Merchant merchant;
    private SaleItem saleItem;
    private List<AdditionalData> additionalData;
}
