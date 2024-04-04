package com.mgm.pd.cp.resortpayment.util.common;


import com.mgm.pd.cp.payment.common.constant.TokenType;
import com.mgm.pd.cp.payment.common.dto.opera.Card;
import com.mgm.pd.cp.payment.common.dto.opera.OperaResponse;
import com.mgm.pd.cp.payment.common.dto.opera.PrintDetails;
import com.mgm.pd.cp.payment.common.dto.opera.TransactionAmount;
import com.mgm.pd.cp.payment.common.model.Payment;
import org.springframework.stereotype.Component;

import java.util.Collections;

/**
 * Converter Class from payment to Opera Response
 */
@Component
public class Converter {
    public OperaResponse convert(Payment payment) {
        TransactionAmount transactionAmount = TransactionAmount.builder()
                //.balanceAmount(payment.getAmount())
                //.requestedAmount(payment.getAuthorizedAmount())
                .authorizedAmount(payment.getAuthorizedAmount())
                .cumulativeAmount(payment.getCumulativeAmount())
                .currencyIndicator(null)
                //.detailedAmount(detailedAmount)
                .build();

        Card card = Card.builder()
                //.cardType(String.valueOf(payment.getIssuerType()))
                .maskedCardNumber(payment.getLast4DigitsOfCard())
                //.cardHolderName(payment.getCardHolderName())
                .startDate(null)
                .expiryDate(null)
                .cardIssuerName(null)
                .cardIssuerIdentification(null)
                //.sequenceNumber(payment.getSequenceNumber())
                .track1(null)
                .track2(null)
                .track3(null)
                //.isTokenized(false)
                .tokenType(TokenType.MGM)
                .tokenValue(payment.getMgmToken())
                .build();

        PrintDetails printDetails = PrintDetails.builder()
                .printKey(null)
                .printName(null)
                .printValue(null)
                .build();

        return OperaResponse.builder()
                .approvalCode(payment.getGatewayAuthCode())
                .responseCode(payment.getGatewayResponseCode())
                .responseReason(payment.getGatewayReasonDescription())
                .transactionAuthChainId(String.valueOf(payment.getAuthChainId()))
                .networkIdentifier(null)
                .originalTransactionIdentifier(null)
                .transactionDateTime(String.valueOf(payment.getCreatedTimeStamp()))
                .transactionAmount(transactionAmount)
                .card(card)
                .printDetails(Collections.singletonList(printDetails))
                .build();
    }
}
