package com.mgm.pd.cp.resortpayment.util.common;


import com.mgm.pd.cp.payment.common.dto.opera.*;
import com.mgm.pd.cp.payment.common.model.Payment;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
public class Converter {
    //TODO: Map null values to actual values
    public OperaResponse convert(Payment payment) {
        GatewayInfo gatewayInfo = GatewayInfo.builder()
                .gatewayTransactionIdentifier(null)
                .gatewayIdentifier(null)
                .build();

        DetailedAmount detailedAmount = DetailedAmount.builder()
                .amount(null)
                .cashBack(null)
                .gratuity(null)
                .fees(null)
                .rebate(null)
                .vat(null)
                .surcharge(null)
                .build();

        TransactionAmount transactionAmount = TransactionAmount.builder()
                .balanceAmount(payment.getBalance())
                .requestedAmount(payment.getAuthAmountRequested())
                .authorizedAmount(payment.getSettleAmount())
                .cumulativeAmount(payment.getAuthTotalAmount())
                .currencyIndicator(payment.getCurrencyIndicator())
                .detailedAmount(detailedAmount)
                .build();

        Card card = Card.builder()
                .cardType(payment.getCardType())
                .maskedCardNumber(null)
                .cardHolderName(null)
                .startDate(null)
                .expiryDate(null)
                .cardIssuerName(null)
                .cardIssuerIdentification(null)
                .sequenceNumber(payment.getSequenceNumber())
                .track1(null)
                .track2(null)
                .track3(null)
                .isTokenized(false)
                .tokenType(null)
                .tokenValue(null)
                .build();

        PrintDetails printDetails = PrintDetails.builder()
                .printKey(null)
                .printName(null)
                .printValue(null)
                .build();

        return OperaResponse.builder()
                .approvalCode(payment.getApprovalCode())
                .responseCode(payment.getReturnCode())
                .responseReason(null)
                .gatewayInfo(gatewayInfo)
                .networkIdentifier(null)
                .originalTransactionIdentifier(null)
                .transactionDateTime(payment.getTransDate())
                .transactionAmount(transactionAmount)
                .card(card)
                .printDetails(Collections.singletonList(printDetails))
                .build();
    }
}
