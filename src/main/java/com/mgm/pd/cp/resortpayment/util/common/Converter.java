package com.mgm.pd.cp.resortpayment.util.common;


import com.mgm.pd.cp.payment.common.dto.opera.*;
import com.mgm.pd.cp.payment.common.model.Payment;
import org.springframework.stereotype.Component;

import java.util.Collections;

/**
 * Converter Class from payment to Opera Response
 */
@Component
public class Converter {
    //TODO: Map null values to actual values
    public OperaResponse convert(Payment payment) {
        GatewayInfo gatewayInfo = GatewayInfo.builder()
                .gatewayTransactionIdentifier(payment.getGatewayChainId())
                .gatewayIdentifier(null)
                .build();

        DetailedAmount detailedAmount = DetailedAmount.builder()
                .amount(payment.getAmount())
                .cashBack(null)
                .gratuity(null)
                .fees(null)
                .rebate(null)
                .vat(null)
                .surcharge(null)
                .build();

        TransactionAmount transactionAmount = TransactionAmount.builder()
                .balanceAmount(payment.getAmount())
                .requestedAmount(payment.getAuthorizedAmount())
                .authorizedAmount(payment.getAuthorizedAmount())
                .cumulativeAmount(payment.getAmount())
                .currencyIndicator(null)
                .detailedAmount(detailedAmount)
                .build();

        Card card = Card.builder()
                .cardType(String.valueOf(payment.getCardEntryMode()))
                .maskedCardNumber(payment.getLast4DigitsOfCard())
                .cardHolderName(payment.getCardHolderName())
                .startDate(null)
                .expiryDate(null)
                .cardIssuerName(null)
                .cardIssuerIdentification(null)
                //.sequenceNumber(payment.getSequenceNumber())
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
                .approvalCode(payment.getGatewayAuthCode())
                .responseCode(payment.getTransactionStatus())
                .responseReason(payment.getGatewayReasonDescription())
                .gatewayInfo(gatewayInfo)
                .networkIdentifier(null)
                .originalTransactionIdentifier(null)
                .transactionDateTime(String.valueOf(payment.getCreatedTimeStamp()))
                .transactionAmount(transactionAmount)
                .card(card)
                .printDetails(Collections.singletonList(printDetails))
                .build();
    }
}
