package com.mgm.pd.cp.resortpayment.util.common;


import com.mgm.pd.cp.payment.common.constant.TokenType;
import com.mgm.pd.cp.payment.common.dto.CPPaymentCardVoidRequest;
import com.mgm.pd.cp.payment.common.dto.CPPaymentProcessingRequest;
import com.mgm.pd.cp.payment.common.dto.common.BaseTransactionDetails;
import com.mgm.pd.cp.payment.common.dto.common.TransactionDetails;
import com.mgm.pd.cp.payment.common.dto.opera.Card;
import com.mgm.pd.cp.payment.common.dto.opera.OperaResponse;
import com.mgm.pd.cp.payment.common.dto.opera.PrintDetails;
import com.mgm.pd.cp.payment.common.dto.opera.TransactionAmount;
import com.mgm.pd.cp.payment.common.model.Payment;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Objects;

/**
 * Converter Class from payment to Opera Response
 */
@Component
public class Converter {
    public <T> OperaResponse convert(Payment payment, T genericRequest) {
        if (genericRequest.getClass().equals(CPPaymentCardVoidRequest.class)) {
            CPPaymentCardVoidRequest request  = (CPPaymentCardVoidRequest) genericRequest;
            String transactionDateTime = request.getTransactionDateTime();
            BaseTransactionDetails transactionDetails = Objects.nonNull(request.getTransactionDetails()) ? request.getTransactionDetails() : new BaseTransactionDetails();
            return getOperaResponse(payment, setCardDetails(payment, transactionDetails), transactionDateTime, request.getResponseReason());
        } else {
            CPPaymentProcessingRequest request = (CPPaymentProcessingRequest) genericRequest;
            String transactionDateTime = request.getTransactionDateTime();
            TransactionDetails transactionDetails = request.getTransactionDetails();
            return getOperaResponse(payment, setCardDetails(payment, transactionDetails), transactionDateTime, request.getResponseReason());
        }
    }

    private static Card setCardDetails(Payment payment, BaseTransactionDetails transactionDetails) {
        Card card = Objects.nonNull(transactionDetails.getCard()) ? transactionDetails.getCard() : new Card();
        Boolean isTokenized = Objects.nonNull(card.getIsTokenized()) ? card.getIsTokenized() : Boolean.TRUE;
        return Card.builder()
                //.cardEntryMode()
                //.cardType(String.valueOf(payment.getIssuerType()))
                .maskedCardNumber(payment.getLast4DigitsOfCard())
                .startDate(null)
                .expiryDate(null)
                .cardIssuerName(null)
                .cardIssuerIdentification(null)
                //.sequenceNumber(payment.getSequenceNumber())
                .track1(null)
                .track2(null)
                .track3(null)
                .isTokenized(isTokenized)
                .tokenType(TokenType.MGM)
                .tokenValue(payment.getMgmToken())
                .build();
    }

    private static OperaResponse getOperaResponse(Payment payment, Card card, String transactionDateTime, String responseReason) {
        return OperaResponse.builder()
                .approvalCode(payment.getPaymentAuthId())
                .responseCode(payment.getGatewayResponseCode())
                .responseReason(responseReason)
                .transactionDateTime(transactionDateTime)
                .transactionAuthChainId(String.valueOf(payment.getAuthChainId()))
                .transactionAmount(getTransactionAmount(payment))
                .card(card)
                .printDetails(Collections.singletonList(getPrintDetails()))
                .build();
    }

    private static PrintDetails getPrintDetails() {
        return PrintDetails.builder()
                .printKey(null)
                .printName(null)
                .printValue(null)
                .build();
    }

    private static TransactionAmount getTransactionAmount(Payment payment) {
        return TransactionAmount.builder()
                //.balanceAmount(payment.getAmount())
                //.requestedAmount(payment.getAuthorizedAmount())
                .authorizedAmount(payment.getAuthorizedAmount())
                .cumulativeAmount(payment.getCumulativeAmount())
                .currencyIndicator(payment.getCurrencyCode())
                //.detailedAmount(detailedAmount)
                .build();
    }
}
