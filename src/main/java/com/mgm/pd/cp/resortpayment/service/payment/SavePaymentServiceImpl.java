package com.mgm.pd.cp.resortpayment.service.payment;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.mgm.pd.cp.payment.common.constant.*;
import com.mgm.pd.cp.payment.common.dto.CPRequestHeaders;
import com.mgm.pd.cp.payment.common.dto.opera.Card;
import com.mgm.pd.cp.payment.common.dto.opera.TransactionAmount;
import com.mgm.pd.cp.payment.common.model.Payment;
import com.mgm.pd.cp.resortpayment.dto.authorize.AuthorizationRouterResponse;
import com.mgm.pd.cp.resortpayment.dto.authorize.CPPaymentAuthorizationRequest;
import com.mgm.pd.cp.resortpayment.dto.capture.CPPaymentCaptureRequest;
import com.mgm.pd.cp.resortpayment.dto.capture.CaptureRouterResponse;
import com.mgm.pd.cp.resortpayment.dto.cardvoid.CPPaymentCardVoidRequest;
import com.mgm.pd.cp.resortpayment.dto.cardvoid.CardVoidRouterResponse;
import com.mgm.pd.cp.resortpayment.dto.common.*;
import com.mgm.pd.cp.resortpayment.dto.incrementalauth.CPPaymentIncrementalAuthRequest;
import com.mgm.pd.cp.resortpayment.dto.incrementalauth.IncrementalAuthorizationRouterResponse;
import com.mgm.pd.cp.resortpayment.dto.refund.CPPaymentRefundRequest;
import com.mgm.pd.cp.resortpayment.dto.refund.RefundRouterResponse;
import com.mgm.pd.cp.resortpayment.repository.PaymentRepository;
import com.mgm.pd.cp.resortpayment.util.common.PaymentProcessingServiceHelper;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import static com.mgm.pd.cp.payment.common.constant.TransactionType.REFUND;

@Service
@AllArgsConstructor
public class SavePaymentServiceImpl implements SavePaymentService {
    private static final Logger logger = LogManager.getLogger(SavePaymentServiceImpl.class);
    public static final String TRANSACTION_TYPE = "Transaction Type is: {}";

    private PaymentRepository paymentRepository;
    private PaymentProcessingServiceHelper helper;
    @Override
    public Payment saveAuthorizationPayment(CPPaymentAuthorizationRequest request, AuthorizationRouterResponse response) throws InvalidFormatException {
        TransactionDetails transactionDetails = request.getTransactionDetails();
        TransactionAmount transactionAmount = transactionDetails.getTransactionAmount();
        Customer customer = Objects.nonNull(transactionDetails.getCustomer()) ? transactionDetails.getCustomer() : new Customer();
        Card card = Objects.nonNull(transactionDetails.getCard()) ? transactionDetails.getCard(): new Card();
        Payment.PaymentBuilder newPayment = Payment.builder();
        String randomId = UUID.randomUUID().toString();
        Address billingAddress = Objects.nonNull(customer.getBillingAddress()) ? customer.getBillingAddress() : new Address();
        CPRequestHeaders headers = request.getHeaders();
        SaleItem saleItem = Objects.nonNull(transactionDetails.getSaleItem()) ? transactionDetails.getSaleItem() : new SaleItem();
        String saleType = saleItem.getSaleType();
        String cardType = Objects.nonNull(card.getCardType()) ? card.getCardType() : null;
        String enumByString = PaymentProcessingServiceHelper.getEnumValueOfCardType(cardType);
        newPayment
                .paymentId(randomId)
                .referenceId(null)
                .groupId(null)
                //.gatewayRelationNumber(headers.getCorrelationId())
                .clientReferenceNumber(saleItem.getSaleReferenceIdentifier())
                .amount(transactionAmount.getRequestedAmount())
                .cumulativeAmount(transactionAmount.getCumulativeAmount())
                .clientId(headers.getClientId())
                .orderType(Objects.nonNull(saleType) ? OrderType.valueOf(saleType) : null)
                .mgmId(null)
                .mgmToken(card.getTokenValue())
                //.cardHolderName(card.getCardHolderName())
                .tenderCategory(null)
                .currencyCode(transactionAmount.getCurrencyIndicator())
                //.last4DigitsOfCard()
                .billingAddress1(billingAddress.getAddressLine())
                .billingAddress2(billingAddress.getStreetName())
                .billingCity(billingAddress.getTownName())
                .billingState(billingAddress.getCountrySubDivision())
                .billingZipCode(billingAddress.getPostCode())
                .billingCountry(billingAddress.getCountry())
                .transactionType(TransactionType.AUTHORIZE)
                //.paymentAuthSource()
                .deferredAuth(null)
                //.createdBy().updatedBy()
                .mgmCorrelationId(headers.getCorrelationId())
                .mgmJourneyId(headers.getJourneyId())
                .mgmTransactionId(headers.getTransactionId())
                .cardEntryMode(card.getCardEntryMode())
                //.avsResponseCode().cvvResponseCode().dccFlag().dccControlNumber().dccAmount().dccBinRate().dccBinCurrency()
                //.processorStatusCode().processorStatusMessage().processorAuthCode()
                .authSubType(request.getTransactionType())
                .tenderType(String.valueOf(TenderType.CREDIT))
                .issuerType(Objects.nonNull(enumByString) ? IssuerType.valueOf(enumByString) : null)
                .updatedTimestamp(LocalDateTime.now());
        helper.getAuthorizationDetailsFromRouterResponse(request, response, newPayment);
        Payment payment = newPayment.build();
        logger.log(Level.INFO, TRANSACTION_TYPE, payment.getTransactionType());
        return  this.paymentRepository.save(payment);
    }

    @Override
    public Payment saveIncrementalAuthorizationPayment(CPPaymentIncrementalAuthRequest request, IncrementalAuthorizationRouterResponse response, Payment initialPayment) throws InvalidFormatException {
        TransactionDetails transactionDetails = request.getTransactionDetails();
        TransactionAmount transactionAmount = transactionDetails.getTransactionAmount();
        Customer customer = Objects.nonNull(transactionDetails.getCustomer()) ? transactionDetails.getCustomer() : new Customer();
        Card card = Objects.nonNull(transactionDetails.getCard()) ? transactionDetails.getCard(): new Card();
        Payment.PaymentBuilder newPayment = Payment.builder();
        String randomId = UUID.randomUUID().toString();
        Address billingAddress = Objects.nonNull(customer.getBillingAddress()) ? customer.getBillingAddress() : new Address();
        CPRequestHeaders headers = request.getHeaders();
        Gateway gatewayId = (Objects.nonNull(initialPayment) && Objects.nonNull(initialPayment.getGatewayId())) ? initialPayment.getGatewayId() : null;
        SaleItem saleItem = Objects.nonNull(transactionDetails.getSaleItem()) ? transactionDetails.getSaleItem() : new SaleItem();
        String saleType = saleItem.getSaleType();
        String cardType = Objects.nonNull(card.getCardType()) ? card.getCardType() : null;
        String enumByString = PaymentProcessingServiceHelper.getEnumValueOfCardType(cardType);
        newPayment
                .paymentId(randomId)
                .referenceId(request.getReferenceId())
                .groupId(null)
                .gatewayId(gatewayId)
                //.gatewayRelationNumber(headers.getCorrelationId())
                .clientReferenceNumber(saleItem.getSaleReferenceIdentifier())
                .amount(transactionAmount.getRequestedAmount())
                .cumulativeAmount(transactionAmount.getCumulativeAmount())
                .clientId(headers.getClientId())
                .orderType(Objects.nonNull(saleType) ? OrderType.valueOf(saleType) : null)
                .mgmId(null)
                .mgmToken(card.getTokenValue())
                //.cardHolderName()
                .tenderCategory(null)
                .currencyCode(transactionAmount.getCurrencyIndicator())
                //.last4DigitsOfCard()
                .billingAddress1(billingAddress.getAddressLine())
                .billingAddress2(billingAddress.getStreetName())
                .billingCity(billingAddress.getTownName())
                .billingState(billingAddress.getCountrySubDivision())
                .billingZipCode(billingAddress.getPostCode())
                .billingCountry(billingAddress.getCountry())
                .transactionType(TransactionType.AUTHORIZE)
                //.paymentAuthSource()
                .deferredAuth(null)
                .mgmCorrelationId(headers.getCorrelationId())
                .mgmJourneyId(headers.getJourneyId())
                .mgmTransactionId(headers.getTransactionId())
                .cardEntryMode(card.getCardEntryMode())
                //.avsResponseCode().cvvResponseCode().dccFlag().dccControlNumber().dccAmount().dccBinRate().dccBinCurrency()
                //.processorStatusCode().processorStatusMessage().processorAuthCode()
                .authSubType(request.getTransactionType())
                .tenderType(String.valueOf(TenderType.CREDIT))
                .issuerType(Objects.nonNull(enumByString) ? IssuerType.valueOf(enumByString) : null)
                .updatedTimestamp(LocalDateTime.now());
        helper.getIncrementalAuthorizationDetailsFromRouterResponse(request, response, newPayment);
        Payment payment = newPayment.build();
        logger.log(Level.INFO, TRANSACTION_TYPE, payment.getTransactionType());
        return this.paymentRepository.save(payment);
    }

    @Override
    public Payment saveCaptureAuthPayment(CPPaymentCaptureRequest request, CaptureRouterResponse response, Payment initialPayment) throws InvalidFormatException {
        TransactionDetails transactionDetails = request.getTransactionDetails();
        TransactionAmount transactionAmount = transactionDetails.getTransactionAmount();
        Customer customer = Objects.nonNull(transactionDetails.getCustomer()) ? transactionDetails.getCustomer() : new Customer();
        Card card = Objects.nonNull(transactionDetails.getCard()) ? transactionDetails.getCard(): new Card();
        Payment.PaymentBuilder newPayment = Payment.builder();
        String string = UUID.randomUUID().toString();
        Address billingAddress = Objects.nonNull(customer.getBillingAddress()) ? customer.getBillingAddress() : new Address();
        CPRequestHeaders headers = request.getHeaders();
        Gateway gatewayId = (Objects.nonNull(initialPayment) && Objects.nonNull(initialPayment.getGatewayId())) ? initialPayment.getGatewayId() : null;
        SaleItem saleItem = Objects.nonNull(transactionDetails.getSaleItem()) ? transactionDetails.getSaleItem() : new SaleItem();
        String saleType = saleItem.getSaleType();
        String cardType = Objects.nonNull(card.getCardType()) ? card.getCardType() : null;
        String enumByString = PaymentProcessingServiceHelper.getEnumValueOfCardType(cardType);
        newPayment
                .paymentId(string)
                .referenceId(request.getReferenceId())
                .groupId(null)
                .gatewayId(gatewayId)
                //.gatewayRelationNumber(headers.getCorrelationId())
                .clientReferenceNumber(saleItem.getSaleReferenceIdentifier())
                .amount(transactionAmount.getRequestedAmount())
                .cumulativeAmount(transactionAmount.getCumulativeAmount())
                .clientId(headers.getClientId())
                .orderType(Objects.nonNull(saleType) ? OrderType.valueOf(saleType) : null)
                .mgmId(null)
                .mgmToken(card.getTokenValue())
                //.cardHolderName(card.getCardHolderName())
                .tenderCategory(null)
                .currencyCode(transactionAmount.getCurrencyIndicator())
                //.last4DigitsOfCard()
                .billingAddress1(billingAddress.getAddressLine())
                .billingAddress2(billingAddress.getStreetName())
                .billingCity(billingAddress.getTownName())
                .billingState(billingAddress.getCountrySubDivision())
                .billingZipCode(billingAddress.getPostCode())
                .billingCountry(billingAddress.getCountry())
                .transactionType(TransactionType.CAPTURE)
                //.paymentAuthSource()
                .deferredAuth(null)
                //.createdBy().updatedBy()
                .mgmCorrelationId(headers.getCorrelationId())
                .mgmJourneyId(headers.getJourneyId())
                .mgmTransactionId(headers.getTransactionId())
                .cardEntryMode(card.getCardEntryMode())
                //.avsResponseCode().cvvResponseCode().dccFlag().dccControlNumber().dccAmount().dccBinRate().dccBinCurrency()
                //.processorStatusCode().processorStatusMessage().processorAuthCode()
                .authSubType(request.getTransactionType())
                .tenderType(String.valueOf(TenderType.CREDIT))
                .issuerType(Objects.nonNull(enumByString) ? IssuerType.valueOf(enumByString) : null)
                .updatedTimestamp(LocalDateTime.now());
        helper.getCaptureDetailsFromRouterResponse(request, response, newPayment);
        Payment payment = newPayment.build();
        logger.log(Level.INFO, TRANSACTION_TYPE, payment.getTransactionType());
        return this.paymentRepository.save(payment);
    }

    @Override
    public Payment saveCardVoidAuthPayment(CPPaymentCardVoidRequest request, CardVoidRouterResponse response, Payment initialPayment) throws InvalidFormatException {
        BaseTransactionDetails transactionDetails = Objects.nonNull(request.getTransactionDetails()) ? request.getTransactionDetails() : new TransactionDetails();
        Card card = Objects.nonNull(transactionDetails.getCard()) ? transactionDetails.getCard() : new Card();
        Payment.PaymentBuilder newPayment = Payment.builder();
        String string = UUID.randomUUID().toString();
        CPRequestHeaders headers = request.getHeaders();
        Gateway gatewayId = (Objects.nonNull(initialPayment) && Objects.nonNull(initialPayment.getGatewayId())) ? initialPayment.getGatewayId() : null;
        SaleItem saleItem = Objects.nonNull(transactionDetails.getSaleItem()) ? transactionDetails.getSaleItem() : new SaleItem();
        String saleType = saleItem.getSaleType();
        String cardType = Objects.nonNull(card.getCardType()) ? card.getCardType() : null;
        String enumByString = PaymentProcessingServiceHelper.getEnumValueOfCardType(cardType);
        newPayment
                .paymentId(string)
                .referenceId(request.getReferenceId())
                .groupId(null)
                .gatewayId(gatewayId)
                //.gatewayRelationNumber(headers.getCorrelationId())
                .clientReferenceNumber(saleItem.getSaleReferenceIdentifier())
                .clientId(headers.getClientId())
                .orderType(Objects.nonNull(saleType) ? OrderType.valueOf(saleType) : null)
                .mgmId(null)
                .mgmToken(card.getTokenValue())
                //.cardHolderName(card.getCardHolderName())
                .tenderCategory(null)
                //.last4DigitsOfCard()
                .transactionType(TransactionType.VOID)
                //.paymentAuthSource()
                .deferredAuth(null)
                //.createdBy().updatedBy()
                .mgmCorrelationId(headers.getCorrelationId())
                .mgmJourneyId(headers.getJourneyId())
                .mgmTransactionId(headers.getTransactionId())
                .cardEntryMode(card.getCardEntryMode())
                .authSubType(request.getTransactionType())
                .tenderType(String.valueOf(TenderType.CREDIT))
                .issuerType(Objects.nonNull(enumByString) ? IssuerType.valueOf(enumByString) : null)
                .updatedTimestamp(LocalDateTime.now());
                //.avsResponseCode().cvvResponseCode().dccFlag().dccControlNumber().dccAmount().dccBinRate().dccBinCurrency()
                //.processorStatusCode().processorStatusMessage().processorAuthCode()
        helper.getVoidDetailsFromRouterResponse(request, response, newPayment);
        Payment payment = newPayment.build();
        logger.log(Level.INFO, TRANSACTION_TYPE, payment.getTransactionType());
        return this.paymentRepository.save(payment);
    }

    @Override
    public Payment saveRefundPayment(CPPaymentRefundRequest request, RefundRouterResponse response, Payment initialPayment) throws InvalidFormatException {
        Payment.PaymentBuilder newPayment = Payment.builder();
        TransactionDetails transactionDetails = request.getTransactionDetails();
        TransactionAmount transactionAmount = transactionDetails.getTransactionAmount();
        Card card = Objects.nonNull(transactionDetails.getCard()) ? transactionDetails.getCard(): new Card();
        String cardType = Objects.nonNull(card.getCardType()) ? card.getCardType() : null;
        Customer customer = Objects.nonNull(transactionDetails.getCustomer()) ? transactionDetails.getCustomer() : new Customer();
        String string = UUID.randomUUID().toString();
        Address billingAddress = Objects.nonNull(customer.getBillingAddress()) ? customer.getBillingAddress() : new Address();
        CPRequestHeaders headers = request.getHeaders();
        SaleItem saleItem = Objects.nonNull(transactionDetails.getSaleItem()) ? transactionDetails.getSaleItem() : new SaleItem();
        String saleType = saleItem.getSaleType();
        String enumByString = PaymentProcessingServiceHelper.getEnumValueOfCardType(cardType);
        newPayment
                .paymentId(string)
                .referenceId(Objects.nonNull(initialPayment) ? initialPayment.getPaymentId() : null)
                .groupId(null)
                //.gatewayRelationNumber(headers.getCorrelationId())
                .clientReferenceNumber(saleItem.getSaleReferenceIdentifier())
                .amount(transactionAmount.getRequestedAmount())
                .cumulativeAmount(transactionAmount.getCumulativeAmount())
                .clientId(headers.getClientId())
                .orderType(Objects.nonNull(saleType) ? OrderType.valueOf(saleType) : null)
                .mgmId(null)
                .mgmToken(card.getTokenValue())
                //.cardHolderName(card.getCardHolderName())
                .tenderCategory(null)
                .currencyCode(transactionAmount.getCurrencyIndicator())
                //.last4DigitsOfCard()
                .billingAddress1(billingAddress.getAddressLine())
                .billingAddress2(billingAddress.getStreetName())
                .billingCity(billingAddress.getTownName())
                .billingState(billingAddress.getCountrySubDivision())
                .billingZipCode(billingAddress.getPostCode())
                .billingCountry(billingAddress.getCountry())
                .transactionType(REFUND)
                //.paymentAuthSource()
                .deferredAuth(null)
                //.createdBy().updatedBy()
                .mgmCorrelationId(headers.getCorrelationId())
                .mgmJourneyId(headers.getJourneyId())
                .mgmTransactionId(headers.getTransactionId())
                .cardEntryMode(card.getCardEntryMode())
                //.avsResponseCode().cvvResponseCode().dccFlag().dccControlNumber().dccAmount().dccBinRate().dccBinCurrency()
                //.processorStatusCode().processorStatusMessage().processorAuthCode()
                .authSubType(request.getTransactionType())
                .tenderType(String.valueOf(TenderType.CREDIT))
                .issuerType(Objects.nonNull(enumByString) ? IssuerType.valueOf(enumByString) : null)
                .updatedTimestamp(LocalDateTime.now());
        helper.getRefundDetailsFromRouterResponse(request, response, newPayment);
        Payment payment = newPayment.build();
        logger.log(Level.INFO, TRANSACTION_TYPE, payment.getTransactionType());
        return this.paymentRepository.save(payment);
    }
}
