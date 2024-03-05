package com.mgm.pd.cp.resortpayment.service.payment;

import com.mgm.pd.cp.payment.common.constant.CardType;
import com.mgm.pd.cp.payment.common.constant.TransactionType;
import com.mgm.pd.cp.payment.common.dto.opera.Card;
import com.mgm.pd.cp.payment.common.dto.opera.DetailedAmount;
import com.mgm.pd.cp.payment.common.dto.opera.TransactionAmount;
import com.mgm.pd.cp.payment.common.model.Payment;
import com.mgm.pd.cp.resortpayment.dto.*;
import com.mgm.pd.cp.resortpayment.dto.authorize.AuthorizationRouterResponse;
import com.mgm.pd.cp.resortpayment.dto.authorize.CPPaymentAuthorizationRequest;
import com.mgm.pd.cp.resortpayment.dto.capture.CPPaymentCaptureRequest;
import com.mgm.pd.cp.resortpayment.dto.capture.CaptureRouterResponse;
import com.mgm.pd.cp.resortpayment.dto.cardvoid.CPPaymentCardVoidRequest;
import com.mgm.pd.cp.resortpayment.dto.cardvoid.CardVoidRouterResponse;
import com.mgm.pd.cp.resortpayment.dto.incrementalauth.CPPaymentIncrementalAuthRequest;
import com.mgm.pd.cp.resortpayment.dto.incrementalauth.IncrementalAuthorizationRouterResponse;
import com.mgm.pd.cp.resortpayment.dto.refund.CPPaymentRefundRequest;
import com.mgm.pd.cp.resortpayment.dto.refund.RefundRouterResponse;
import com.mgm.pd.cp.resortpayment.repository.PaymentRepository;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.UUID;

import static com.mgm.pd.cp.payment.common.constant.TransactionType.REFUND;

@Service
@AllArgsConstructor
public class SavePaymentServiceImpl implements SavePaymentService {
    private static final Logger logger = LogManager.getLogger(SavePaymentServiceImpl.class);

    private PaymentRepository paymentRepository;

    @Override
    public Payment saveIncrementalAuthorizationPayment(CPPaymentIncrementalAuthRequest request, IncrementalAuthorizationRouterResponse response) {
        TransactionDetails transactionDetails = request.getTransactionDetails();
        TransactionAmount transactionAmount = transactionDetails.getTransactionAmount();
        Customer customer = transactionDetails.getCustomer();
        CurrencyConversion currencyConversion = transactionDetails.getCurrencyConversion();
        Card card = transactionDetails.getCard();
        Payment.PaymentBuilder newPayment = Payment.builder();
        String randomId = UUID.randomUUID().toString();
        DetailedAmount detailedAmount = transactionAmount.getDetailedAmount();
        Address billingAddress = customer.getBillingAddress();
        newPayment
                .paymentId(randomId)
                .referenceId(String.valueOf(request.getReferenceId()))
                .groupId(null)
                .gatewayRelationNumber(request.getCorelationId())
                .gatewayChainId(String.valueOf(request.getIncrementalAuthInvoiceId()))
                .clientReferenceNumber(request.getTransactionIdentifier())
                .amount(detailedAmount.getAmount())
                //.authChainId(request.getIncrementalAuthInvoiceId())
                //TODO: check if it is coming in which request parameter
                //.gatewayId()
                .clientId(request.getClientID())
                //.orderType()
                .mgmId(null)
                .mgmToken(card.getMaskedCardNumber())
                .cardHolderName(card.getCardHolderName())
                .tenderCategory(null)
                .currencyCode(currencyConversion.getBinCurrencyCode())
                //.last4DigitsOfCard()
                .billingAddress1(billingAddress.getAddressLine())
                .billingAddress2(billingAddress.getStreetName())
                .billingCity(billingAddress.getTownName())
                .billingState(billingAddress.getTownName())
                .billingZipCode(billingAddress.getPostCode())
                .billingCountry(billingAddress.getCountry())
                //.clerkId()
                .transactionType(TransactionType.INCREMENTAL_AUTH)
                //.gatewayReasonCode().gatewayReasonDescription().gatewayResponseCode().gatewayAuthSource()
                .deferredAuth(null)
                .createdTimeStamp(convertToTimestamp(request.getTransactionDateTime()))
                //.createdBy().updatedBy()
                .correlationId(request.getCorelationId())
                //.journeyId().transactionSessionId()
                //.cardEntryMode().avsResponseCode().cvvResponseCode().dccFlag().dccControlNumber().dccAmount().dccBinRate().dccBinCurrency()
                //.processorStatusCode().processorStatusMessage().processorAuthCode()
                .authSubType(request.getTransactionType());
        if (Objects.nonNull(response)) {
            String transDate = response.getTransDate();
            String cardType = response.getCardType();
            newPayment
                    .authorizedAmount(response.getTotalAuthAmount())
                    //.tenderType(TenderType.valueOf(response.getCardType()))
                    .issuerType(Objects.nonNull(cardType) ? CardType.valueOf(cardType) : null)
                    .gatewayAuthCode(response.getApprovalCode())
                    .transactionStatus(response.getReturnCode())
                    .updatedTimestamp(Objects.nonNull(transDate) ? convertToTimestamp(transDate) : null)
                    .authChainId(Long.valueOf(response.getVendorTranID()));
        }
        Payment payment = newPayment.build();
        logger.log(Level.INFO, "Transaction Type is: " + payment.getTransactionType());
        return this.paymentRepository.save(payment);
    }

    @Override
    public Payment saveAuthorizationPayment(CPPaymentAuthorizationRequest request, AuthorizationRouterResponse response) {
        TransactionDetails transactionDetails = request.getTransactionDetails();
        TransactionAmount transactionAmount = transactionDetails.getTransactionAmount();
        Customer customer = transactionDetails.getCustomer();
        CurrencyConversion currencyConversion = transactionDetails.getCurrencyConversion();
        Card card = transactionDetails.getCard();
        Payment.PaymentBuilder newPayment = Payment.builder();
        String randomId = UUID.randomUUID().toString();
        DetailedAmount detailedAmount = transactionAmount.getDetailedAmount();
        Address billingAddress = customer.getBillingAddress();
        newPayment
                .paymentId(randomId)
                .referenceId(null)
                .groupId(null)
                .gatewayRelationNumber(request.getCorelationId())
                .gatewayChainId(String.valueOf(request.getIncrementalAuthInvoiceId()))
                .clientReferenceNumber(request.getTransactionIdentifier())
                .amount(detailedAmount.getAmount())
                .authChainId(request.getIncrementalAuthInvoiceId())
                //TODO: check if it is coming in which request parameter
                //.gatewayId()
                .clientId(request.getClientID())
                //.orderType()
                .mgmId(null)
                .mgmToken(card.getMaskedCardNumber())
                .cardHolderName(card.getCardHolderName())
                .tenderCategory(null)
                .currencyCode(currencyConversion.getBinCurrencyCode())
                //.last4DigitsOfCard()
                .billingAddress1(billingAddress.getAddressLine())
                .billingAddress2(billingAddress.getStreetName())
                .billingCity(billingAddress.getTownName())
                .billingState(billingAddress.getTownName())
                .billingZipCode(billingAddress.getPostCode())
                .billingCountry(billingAddress.getCountry())
                //.clerkId()
                .transactionType(TransactionType.INIT_AUTH_CNP)
                //.gatewayReasonCode().gatewayReasonDescription().gatewayResponseCode().gatewayAuthSource()
                .deferredAuth(null)
                .createdTimeStamp(convertToTimestamp(request.getTransactionDateTime()))
                //.createdBy().updatedBy()
                .correlationId(request.getCorelationId())
                //.journeyId().transactionSessionId()
                //.cardEntryMode().avsResponseCode().cvvResponseCode().dccFlag().dccControlNumber().dccAmount().dccBinRate().dccBinCurrency()
                //.processorStatusCode().processorStatusMessage().processorAuthCode()
                .authSubType(request.getTransactionType());
        if (Objects.nonNull(response)) {
            String transDate = response.getTransDate();
            String cardType = response.getCardType();
            newPayment
                    .authorizedAmount(response.getTotalAuthAmount())
                    //.tenderType(TenderType.valueOf(response.getCardType()))
                    .issuerType(Objects.nonNull(cardType) ? CardType.valueOf(cardType) : null)
                    .gatewayAuthCode(response.getApprovalCode())
                    .transactionStatus(response.getReturnCode())
                    .updatedTimestamp(Objects.nonNull(transDate) ? convertToTimestamp(transDate) : null);
        }
        Payment payment = newPayment.build();
        logger.log(Level.INFO, "Transaction Type is: " + payment.getTransactionType());
        return  this.paymentRepository.save(payment);
    }

    private LocalDateTime convertToTimestamp(String transactionDateTime) {
        transactionDateTime = transactionDateTime.substring(0, 19);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd['T']HH:mm:ss['Z']");
        return LocalDateTime.parse(transactionDateTime, formatter);
    }

    @Override
    public Payment saveCaptureAuthPayment(CPPaymentCaptureRequest request, CaptureRouterResponse response, Payment initialAuthAmount) {
        TransactionDetails transactionDetails = request.getTransactionDetails();
        TransactionAmount transactionAmount = transactionDetails.getTransactionAmount();
        Customer customer = transactionDetails.getCustomer();
        CurrencyConversion currencyConversion = transactionDetails.getCurrencyConversion();
        Card card = transactionDetails.getCard();
        Payment.PaymentBuilder newPayment = Payment.builder();
        String string = UUID.randomUUID().toString();
        DetailedAmount detailedAmount = transactionAmount.getDetailedAmount();
        Address billingAddress = customer.getBillingAddress();
        newPayment
                .paymentId(string)
                .referenceId(String.valueOf(request.getReferenceId()))
                .groupId(null)
                .gatewayRelationNumber(request.getCorelationId())
                .gatewayChainId(String.valueOf(request.getIncrementalAuthInvoiceId()))
                .clientReferenceNumber(request.getTransactionIdentifier())
                .amount(detailedAmount.getAmount())
                .authChainId(request.getIncrementalAuthInvoiceId())
                //TODO: check if it is coming in which request parameter
                //.gatewayId()
                .clientId(request.getClientID())
                //.orderType()
                .mgmId(null)
                .mgmToken(card.getMaskedCardNumber())
                .cardHolderName(card.getCardHolderName())
                .tenderCategory(null)
                .currencyCode(currencyConversion.getBinCurrencyCode())
                //.last4DigitsOfCard()
                .billingAddress1(billingAddress.getAddressLine())
                .billingAddress2(billingAddress.getStreetName())
                .billingCity(billingAddress.getTownName())
                .billingState(billingAddress.getTownName())
                .billingZipCode(billingAddress.getPostCode())
                .billingCountry(billingAddress.getCountry())
                //.clerkId()
                .transactionType(TransactionType.CAPTURE)
                //.gatewayReasonCode().gatewayReasonDescription().gatewayResponseCode().gatewayAuthSource()
                .deferredAuth(null)
                .createdTimeStamp(convertToTimestamp(request.getTransactionDateTime()))
                //.createdBy().updatedBy()
                .correlationId(request.getCorelationId())
                //.journeyId().transactionSessionId()
                //.cardEntryMode().avsResponseCode().cvvResponseCode().dccFlag().dccControlNumber().dccAmount().dccBinRate().dccBinCurrency()
                //.processorStatusCode().processorStatusMessage().processorAuthCode()
                .authSubType(request.getTransactionType());
        if (Objects.nonNull(response)) {
            String transDate = response.getTransDate();
            CardType cardType = response.getCardType();
            newPayment
                    .authorizedAmount(response.getTotalAuthAmount())
                    //.tenderType(TenderType.valueOf(response.getCardType()))
                    .issuerType(Objects.nonNull(cardType) ? cardType : null)
                    .gatewayAuthCode(response.getApprovalCode())
                    .transactionStatus(response.getReturnCode())
                    .updatedTimestamp(Objects.nonNull(transDate) ? convertToTimestamp(transDate) : null);
        }
        Payment payment = newPayment.build();
        logger.log(Level.INFO, "Transaction Type is: " + payment.getTransactionType());
        return this.paymentRepository.save(payment);
    }

    @Override
    public Payment saveCardVoidAuthPayment(CPPaymentCardVoidRequest request, CardVoidRouterResponse response) {
        BaseTransactionDetails transactionDetails = request.getTransactionDetails();
        Card card = new Card();
        if (Objects.nonNull(transactionDetails)) {
            card = transactionDetails.getCard();
        }
        Payment.PaymentBuilder newPayment = Payment.builder();
        String string = UUID.randomUUID().toString();
        newPayment
                .paymentId(string)
                .referenceId(String.valueOf(request.getReferenceId()))
                .groupId(null)
                .gatewayRelationNumber(request.getCorelationId())
                .gatewayChainId(String.valueOf(request.getIncrementalAuthInvoiceId()))
                .clientReferenceNumber(request.getTransactionIdentifier())
                //.amount(detailedAmount.getAmount())
                .authChainId(request.getIncrementalAuthInvoiceId())
                //TODO: check if it is coming in which request parameter
                //.gatewayId()
                .clientId(request.getClientID())
                //.orderType()
                .mgmId(null)
                .mgmToken(card.getMaskedCardNumber())
                .cardHolderName(card.getCardHolderName())
                .tenderCategory(null)
                //.currencyCode(currencyConversion.getBinCurrencyCode())
                //.last4DigitsOfCard()
                /*.billingAddress1(billingAddress.getAddressLine())
                .billingAddress2(billingAddress.getStreetName())
                .billingCity(billingAddress.getTownName())
                .billingState(billingAddress.getTownName())
                .billingZipCode(billingAddress.getPostCode())
                .billingCountry(billingAddress.getCountry())*/
                //.clerkId()
                .transactionType(TransactionType.CARD_VOID)
                //.gatewayReasonCode().gatewayReasonDescription().gatewayResponseCode().gatewayAuthSource()
                .deferredAuth(null)
                .createdTimeStamp(convertToTimestamp(request.getTransactionDateTime()))
                //.createdBy().updatedBy()
                .correlationId(request.getCorelationId());
                //.journeyId().transactionSessionId()
                //.cardEntryMode().avsResponseCode().cvvResponseCode().dccFlag().dccControlNumber().dccAmount().dccBinRate().dccBinCurrency()
                //.processorStatusCode().processorStatusMessage().processorAuthCode()
                //.authSubType(AuthType.valueOf(request.getTransactionType()));
        if (Objects.nonNull(response)) {
            String transDate = response.getTransDate();
            newPayment
                    .authorizedAmount(response.getTotalAuthAmount())
                    //.tenderType(TenderType.valueOf(response.getCardType()))
                    .issuerType(response.getCardType())
                    .gatewayAuthCode(response.getApprovalCode())
                    .transactionStatus(response.getReturnCode())
                    .updatedTimestamp(Objects.nonNull(transDate) ? convertToTimestamp(transDate) : null);
        }
        Payment payment = newPayment.build();
        logger.log(Level.INFO, "Transaction Type is: " + payment.getTransactionType());
        return this.paymentRepository.save(payment);
    }

    @Override
    public Payment saveRefundPayment(CPPaymentRefundRequest request, RefundRouterResponse response) {
        Payment.PaymentBuilder newPayment = Payment.builder();
        TransactionDetails transactionDetails = request.getTransactionDetails();
        TransactionAmount transactionAmount = transactionDetails.getTransactionAmount();
        CurrencyConversion currencyConversion = transactionDetails.getCurrencyConversion();
        Card card = transactionDetails.getCard();
        Customer customer = transactionDetails.getCustomer();
        String string = UUID.randomUUID().toString();
        DetailedAmount detailedAmount = transactionAmount.getDetailedAmount();
        Address billingAddress = customer.getBillingAddress();
        newPayment
                .paymentId(string)
                .referenceId(String.valueOf(request.getReferenceId()))
                .groupId(null)
                .gatewayRelationNumber(request.getCorelationId())
                .gatewayChainId(String.valueOf(request.getIncrementalAuthInvoiceId()))
                .clientReferenceNumber(request.getTransactionIdentifier())
                .amount(detailedAmount.getAmount())
                .authChainId(request.getIncrementalAuthInvoiceId())
                //TODO: check if it is coming in which request parameter
                //.gatewayId()
                .clientId(request.getClientID())
                //.orderType()
                .mgmId(null)
                .mgmToken(card.getMaskedCardNumber())
                .cardHolderName(card.getCardHolderName())
                .tenderCategory(null)
                .currencyCode(currencyConversion.getBinCurrencyCode())
                //.last4DigitsOfCard()
                .billingAddress1(billingAddress.getAddressLine())
                .billingAddress2(billingAddress.getStreetName())
                .billingCity(billingAddress.getTownName())
                .billingState(billingAddress.getTownName())
                .billingZipCode(billingAddress.getPostCode())
                .billingCountry(billingAddress.getCountry())
                //.clerkId()
                .transactionType(REFUND)
                //.gatewayReasonCode().gatewayReasonDescription().gatewayResponseCode().gatewayAuthSource()
                .deferredAuth(null)
                .createdTimeStamp(convertToTimestamp(request.getTransactionDateTime()))
                //.createdBy().updatedBy()
                .correlationId(request.getCorelationId())
                //.journeyId().transactionSessionId()
                //.cardEntryMode().avsResponseCode().cvvResponseCode().dccFlag().dccControlNumber().dccAmount().dccBinRate().dccBinCurrency()
                //.processorStatusCode().processorStatusMessage().processorAuthCode()
                .authSubType(request.getTransactionType());
        if (Objects.nonNull(response)) {
            String transDate = response.getTransDate();
            newPayment
                    .authorizedAmount(response.getTotalAuthAmount())
                    //.tenderType(TenderType.valueOf(response.getCardType()))
                    .issuerType(response.getCardType())
                    .gatewayAuthCode(response.getApprovalCode())
                    .transactionStatus(response.getReturnCode())
                    .updatedTimestamp(Objects.nonNull(transDate) ? convertToTimestamp(transDate) : null);
        }
        Payment payment = newPayment.build();
        logger.log(Level.INFO, "Transaction Type is: " + payment.getTransactionType());
        return this.paymentRepository.save(payment);
    }

    private TransactionType setCaptureTransactionType(Double initialAuthAmount, Double totalAuthAmount) {
        TransactionType captureTransactionType = null;
        if (Objects.nonNull(initialAuthAmount) && Objects.nonNull(totalAuthAmount)) {
            if(totalAuthAmount > initialAuthAmount) {
                captureTransactionType =  TransactionType.CAPTURE_ADDITIONAL_AUTH;
            }
            if (totalAuthAmount < initialAuthAmount) {
                captureTransactionType = TransactionType.CAPTURE_PARTIAL_VOID;
            }
        }
        return captureTransactionType;
    }
}
