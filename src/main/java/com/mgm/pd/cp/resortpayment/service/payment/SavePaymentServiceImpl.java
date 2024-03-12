package com.mgm.pd.cp.resortpayment.service.payment;

import com.mgm.pd.cp.payment.common.constant.CardType;
import com.mgm.pd.cp.payment.common.constant.OrderType;
import com.mgm.pd.cp.payment.common.constant.TenderType;
import com.mgm.pd.cp.payment.common.constant.TransactionType;
import com.mgm.pd.cp.payment.common.dto.CPRequestHeaders;
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

import static com.mgm.pd.cp.payment.common.constant.ApplicationConstants.FAILURE_MESSAGE;
import static com.mgm.pd.cp.payment.common.constant.ApplicationConstants.SUCCESS_MESSAGE;
import static com.mgm.pd.cp.payment.common.constant.ReturnCode.Approved;
import static com.mgm.pd.cp.payment.common.constant.ReturnCode.Declined;
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
        Address billingAddress = Objects.nonNull(customer.getBillingAddress()) ? customer.getBillingAddress() : new Address();
        CPRequestHeaders headers = request.getHeaders();
        newPayment
                .paymentId(randomId)
                .referenceId(String.valueOf(request.getReferenceId()))
                .groupId(null)
                //.gatewayRelationNumber(headers.getCorrelationId())
                .clientReferenceNumber(transactionDetails.getSaleItem().getSaleReferenceIdentifier())
                .amount(detailedAmount.getAmount())
                .clientId(headers.getClientId())
                .orderType(OrderType.Hotel)
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
                .clerkId(transactionDetails.getMerchant().getClerkIdentifier())
                .transactionType(TransactionType.AUTHORIZATION)
                //.gatewayReasonCode().gatewayReasonDescription().gatewayAuthSource()
                .deferredAuth(null)
                .createdTimeStamp(convertToTimestamp(request.getTransactionDateTime()))
                //.createdBy().updatedBy()
                .correlationId(headers.getCorrelationId())
                .journeyId(headers.getJourneyId())
                .transactionSessionId(headers.getTransactionId())
                .cardEntryMode(card.getCardEntryMode())
                //.avsResponseCode().cvvResponseCode().dccFlag().dccControlNumber().dccAmount().dccBinRate().dccBinCurrency()
                //.processorStatusCode().processorStatusMessage().processorAuthCode()
                .authSubType(request.getTransactionType())
                .tenderType(TenderType.valueOf(card.getCardType()));
        if (Objects.nonNull(response)) {
            String transDate = response.getTransDate();
            String cardType = response.getCardType();
            String vendorTranID = response.getVendorTranID();
            String returnCode = Objects.nonNull(response.getReturnCode()) ? response.getReturnCode() : "";
            newPayment
                    .authorizedAmount(response.getTotalAuthAmount())
                    //.gatewayId()
                    .issuerType(Objects.nonNull(cardType) ? CardType.valueOf(cardType) : null)
                    .gatewayAuthCode(response.getApprovalCode())
                    .gatewayResponseCode(returnCode)
                    .transactionStatus((returnCode.equals(Approved.name()) || returnCode.equals(Declined.name())) ? SUCCESS_MESSAGE : FAILURE_MESSAGE)
                    .updatedTimestamp(Objects.nonNull(transDate) ? convertToTimestamp(transDate) : null)
                    .authChainId(Objects.nonNull(vendorTranID) ? Long.valueOf(vendorTranID) : null)
                    .gatewayChainId(vendorTranID);
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
        Address billingAddress = Objects.nonNull(customer.getBillingAddress()) ? customer.getBillingAddress() : new Address();
        CPRequestHeaders headers = request.getHeaders();
        newPayment
                .paymentId(randomId)
                .referenceId(null)
                .groupId(null)
                //.gatewayRelationNumber(headers.getCorrelationId())
                .clientReferenceNumber(transactionDetails.getSaleItem().getSaleReferenceIdentifier())
                .amount(detailedAmount.getAmount())
                .clientId(headers.getClientId())
                .orderType(OrderType.Hotel)
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
                .clerkId(transactionDetails.getMerchant().getClerkIdentifier())
                .transactionType(TransactionType.AUTHORIZATION)
                //.gatewayReasonCode().gatewayReasonDescription()
                //.gatewayAuthSource()
                .deferredAuth(null)
                .createdTimeStamp(convertToTimestamp(request.getTransactionDateTime()))
                //.createdBy().updatedBy()
                .correlationId(headers.getCorrelationId())
                .journeyId(headers.getJourneyId())
                .transactionSessionId(headers.getTransactionId())
                .cardEntryMode(card.getCardEntryMode())
                //.avsResponseCode().cvvResponseCode().dccFlag().dccControlNumber().dccAmount().dccBinRate().dccBinCurrency()
                //.processorStatusCode().processorStatusMessage().processorAuthCode()
                .authSubType(request.getTransactionType())
                .tenderType(TenderType.valueOf(card.getCardType()));
        if (Objects.nonNull(response)) {
            String transDate = response.getTransDate();
            String cardType = response.getCardType();
            String returnCode = Objects.nonNull(response.getReturnCode()) ? response.getReturnCode() : "";
            String vendorTranID = response.getVendorTranID();
            newPayment
                    .gatewayChainId(vendorTranID)
                    .authChainId(Objects.nonNull(vendorTranID) ? Long.valueOf(vendorTranID) : null)
                    .authorizedAmount(response.getTotalAuthAmount())
                    //.gatewayId()
                    .issuerType(Objects.nonNull(cardType) ? CardType.valueOf(cardType) : null)
                    .gatewayAuthCode(response.getApprovalCode())
                    .gatewayResponseCode(returnCode)
                    .transactionStatus((returnCode.equals(Approved.name()) || returnCode.equals(Declined.name())) ? SUCCESS_MESSAGE : FAILURE_MESSAGE)
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
        Address billingAddress = Objects.nonNull(customer.getBillingAddress()) ? customer.getBillingAddress() : new Address();
        CPRequestHeaders headers = request.getHeaders();
        newPayment
                .paymentId(string)
                .referenceId(String.valueOf(request.getReferenceId()))
                .groupId(null)
                //.gatewayRelationNumber(headers.getCorrelationId())
                .clientReferenceNumber(transactionDetails.getSaleItem().getSaleReferenceIdentifier())
                .amount(detailedAmount.getAmount())
                .authChainId(request.getAuthChainId())
                .clientId(headers.getClientId())
                .orderType(OrderType.Hotel)
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
                .clerkId(transactionDetails.getMerchant().getClerkIdentifier())
                .transactionType(TransactionType.CAPTURE)
                //.gatewayReasonCode().gatewayReasonDescription().gatewayAuthSource()
                .deferredAuth(null)
                .createdTimeStamp(convertToTimestamp(request.getTransactionDateTime()))
                //.createdBy().updatedBy()
                .correlationId(headers.getCorrelationId())
                .journeyId(headers.getJourneyId())
                .transactionSessionId(headers.getTransactionId())
                .cardEntryMode(card.getCardEntryMode())
                //.avsResponseCode().cvvResponseCode().dccFlag().dccControlNumber().dccAmount().dccBinRate().dccBinCurrency()
                //.processorStatusCode().processorStatusMessage().processorAuthCode()
                .authSubType(request.getTransactionType())
                .tenderType(TenderType.valueOf(card.getCardType()));
        if (Objects.nonNull(response)) {
            String transDate = response.getTransDate();
            CardType cardType = response.getCardType();
            String returnCode = Objects.nonNull(response.getReturnCode()) ? response.getReturnCode() : "";
            String vendorTranID = response.getVendorTranID();
            newPayment
                    .gatewayChainId(vendorTranID)
                    .authorizedAmount(response.getTotalAuthAmount())
                    //.gatewayId()
                    .issuerType(Objects.nonNull(cardType) ? cardType : null)
                    .gatewayAuthCode(response.getApprovalCode())
                    .gatewayResponseCode(returnCode)
                    .transactionStatus((returnCode.equals(Approved.name()) || returnCode.equals(Declined.name())) ? SUCCESS_MESSAGE : FAILURE_MESSAGE)
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
        CPRequestHeaders headers = request.getHeaders();
        newPayment
                .paymentId(string)
                .referenceId(String.valueOf(request.getReferenceId()))
                .groupId(null)
                //.gatewayRelationNumber(headers.getCorrelationId())
                .clientReferenceNumber(transactionDetails.getSaleItem().getSaleReferenceIdentifier())
                //.amount(detailedAmount.getAmount())
                .authChainId(request.getAuthChainId())
                .clientId(headers.getClientId())
                .orderType(OrderType.Hotel)
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
                .clerkId(transactionDetails.getMerchant().getClerkIdentifier())
                .transactionType(TransactionType.VOID)
                //.gatewayReasonCode().gatewayReasonDescription().gatewayAuthSource()
                .deferredAuth(null)
                .createdTimeStamp(convertToTimestamp(request.getTransactionDateTime()))
                //.createdBy().updatedBy()
                .correlationId(headers.getCorrelationId())
                .journeyId(headers.getJourneyId())
                .transactionSessionId(headers.getTransactionId())
                .cardEntryMode(card.getCardEntryMode())
                .tenderType(TenderType.valueOf(card.getCardType()));
                //.avsResponseCode().cvvResponseCode().dccFlag().dccControlNumber().dccAmount().dccBinRate().dccBinCurrency()
                //.processorStatusCode().processorStatusMessage().processorAuthCode()
                //.authSubType(AuthType.valueOf(request.getTransactionType()));
        if (Objects.nonNull(response)) {
            String transDate = response.getTransDate();
            String returnCode = Objects.nonNull(response.getReturnCode()) ? response.getReturnCode() : "";
            String vendorTranID = response.getVendorTranID();
            newPayment
                    .gatewayChainId(vendorTranID)
                    .authorizedAmount(response.getTotalAuthAmount())
                    //.gatewayId()
                    .issuerType(response.getCardType())
                    .gatewayAuthCode(response.getApprovalCode())
                    .gatewayResponseCode(returnCode)
                    .transactionStatus((returnCode.equals(Approved.name()) || returnCode.equals(Declined.name())) ? SUCCESS_MESSAGE : FAILURE_MESSAGE)
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
        Address billingAddress = Objects.nonNull(customer.getBillingAddress()) ? customer.getBillingAddress() : new Address();
        CPRequestHeaders headers = request.getHeaders();
        newPayment
                .paymentId(string)
                .referenceId(String.valueOf(request.getReferenceId()))
                .groupId(null)
                //.gatewayRelationNumber(headers.getCorrelationId())
                .clientReferenceNumber(transactionDetails.getSaleItem().getSaleReferenceIdentifier())
                .amount(detailedAmount.getAmount())
                .authChainId(request.getAuthChainId())
                //.gatewayId()
                .clientId(headers.getClientId())
                .orderType(OrderType.Hotel)
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
                .clerkId(transactionDetails.getMerchant().getClerkIdentifier())
                .transactionType(REFUND)
                //.gatewayReasonCode().gatewayReasonDescription().gatewayAuthSource()
                .deferredAuth(null)
                .createdTimeStamp(convertToTimestamp(request.getTransactionDateTime()))
                //.createdBy().updatedBy()
                .correlationId(headers.getCorrelationId())
                .journeyId(headers.getJourneyId())
                .transactionSessionId(headers.getTransactionId())
                .cardEntryMode(card.getCardEntryMode())
                //.avsResponseCode().cvvResponseCode().dccFlag().dccControlNumber().dccAmount().dccBinRate().dccBinCurrency()
                //.processorStatusCode().processorStatusMessage().processorAuthCode()
                .authSubType(request.getTransactionType())
                .tenderType(TenderType.valueOf(card.getCardType()));
        if (Objects.nonNull(response)) {
            String transDate = response.getTransDate();
            String returnCode = Objects.nonNull(response.getReturnCode()) ? response.getReturnCode() : "";
            String vendorTranID = response.getVendorTranID();
            newPayment
                    .gatewayChainId(vendorTranID)
                    .authorizedAmount(response.getTotalAuthAmount())
                    .issuerType(response.getCardType())
                    .gatewayAuthCode(response.getApprovalCode())
                    .gatewayResponseCode(returnCode)
                    .transactionStatus((returnCode.equals(Approved.name()) || returnCode.equals(Declined.name())) ? SUCCESS_MESSAGE : FAILURE_MESSAGE)
                    .updatedTimestamp(Objects.nonNull(transDate) ? convertToTimestamp(transDate) : null);
        }
        Payment payment = newPayment.build();
        logger.log(Level.INFO, "Transaction Type is: " + payment.getTransactionType());
        return this.paymentRepository.save(payment);
    }
}
