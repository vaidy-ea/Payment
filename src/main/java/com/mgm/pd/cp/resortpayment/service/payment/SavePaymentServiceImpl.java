package com.mgm.pd.cp.resortpayment.service.payment;

import com.mgm.pd.cp.payment.common.constant.*;
import com.mgm.pd.cp.payment.common.dto.CPRequestHeaders;
import com.mgm.pd.cp.payment.common.dto.opera.Card;
import com.mgm.pd.cp.payment.common.dto.opera.DetailedAmount;
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

import static com.mgm.pd.cp.payment.common.constant.ApplicationConstants.FAILURE_MESSAGE;
import static com.mgm.pd.cp.payment.common.constant.ApplicationConstants.SUCCESS_MESSAGE;
import static com.mgm.pd.cp.payment.common.constant.ReturnCode.Approved;
import static com.mgm.pd.cp.payment.common.constant.TransactionType.REFUND;

@Service
@AllArgsConstructor
public class SavePaymentServiceImpl implements SavePaymentService {
    private static final Logger logger = LogManager.getLogger(SavePaymentServiceImpl.class);

    private PaymentRepository paymentRepository;
    private PaymentProcessingServiceHelper helper;

    @Override
    public Payment saveIncrementalAuthorizationPayment(CPPaymentIncrementalAuthRequest request, IncrementalAuthorizationRouterResponse response, Payment initialPayment) {
        TransactionDetails transactionDetails = request.getTransactionDetails();
        TransactionAmount transactionAmount = transactionDetails.getTransactionAmount();
        Customer customer = transactionDetails.getCustomer();
        CurrencyConversion currencyConversion = transactionDetails.getCurrencyConversion();
        CurrencyConversion cc = Objects.nonNull(currencyConversion) ? currencyConversion : new CurrencyConversion();
        Card card = transactionDetails.getCard();
        Payment.PaymentBuilder newPayment = Payment.builder();
        String randomId = UUID.randomUUID().toString();
        DetailedAmount detailedAmount = transactionAmount.getDetailedAmount();
        Address billingAddress = Objects.nonNull(customer.getBillingAddress()) ? customer.getBillingAddress() : new Address();
        CPRequestHeaders headers = request.getHeaders();
        String authChainId = request.getAuthChainId();
        Gateway gatewayId = (Objects.nonNull(initialPayment) && Objects.nonNull(initialPayment.getGatewayId())) ? initialPayment.getGatewayId() : null;
        SaleItem<?> saleItem = transactionDetails.getSaleItem();
        String saleType = saleItem.getSaleType();
        newPayment
                .paymentId(randomId)
                .referenceId(String.valueOf(request.getReferenceId()))
                .groupId(null)
                .gatewayId(gatewayId)
                //.gatewayRelationNumber(headers.getCorrelationId())
                .clientReferenceNumber(saleItem.getSaleReferenceIdentifier())
                .amount(detailedAmount.getAmount())
                .gatewayChainId(Objects.nonNull(authChainId) ? authChainId.replaceFirst("^0+(?!$)", "") : null)
                .authChainId(Objects.nonNull(authChainId) ? Long.valueOf(authChainId) : null)
                .clientId(headers.getClientId())
                .orderType(Objects.nonNull(saleType) ? OrderType.valueOf(saleType) : null)
                .mgmId(null)
                .mgmToken(card.getMaskedCardNumber())
                .cardHolderName(card.getCardHolderName())
                .tenderCategory(null)
                .currencyCode(cc.getBinCurrencyCode())
                //.last4DigitsOfCard()
                .billingAddress1(billingAddress.getAddressLine())
                .billingAddress2(billingAddress.getStreetName())
                .billingCity(billingAddress.getTownName())
                .billingState(billingAddress.getTownName())
                .billingZipCode(billingAddress.getPostCode())
                .billingCountry(billingAddress.getCountry())
                .clerkId(transactionDetails.getMerchant().getClerkIdentifier())
                .transactionType(TransactionType.AUTHORIZE)
                //.gatewayReasonCode().gatewayReasonDescription().gatewayAuthSource()
                .deferredAuth(null)
                .createdTimeStamp(helper.convertToTimestamp(request.getTransactionDateTime()))
                //.createdBy().updatedBy()
                .correlationId(headers.getCorrelationId())
                .journeyId(headers.getJourneyId())
                .transactionSessionId(headers.getTransactionId())
                .cardEntryMode(card.getCardEntryMode())
                //.avsResponseCode().cvvResponseCode().dccFlag().dccControlNumber().dccAmount().dccBinRate().dccBinCurrency()
                //.processorStatusCode().processorStatusMessage().processorAuthCode()
                .authSubType(request.getTransactionType())
                .tenderType(TenderType.valueOf(card.getCardType()))
                .updatedTimestamp(LocalDateTime.now());
        if (Objects.nonNull(response)) {
            String transDate = response.getTransDate();
            String cardType = response.getCardType();
            String returnCode = Objects.nonNull(response.getReturnCode()) ? response.getReturnCode() : "";
            newPayment
                    .authorizedAmount(response.getTotalAuthAmount())
                    .issuerType(Objects.nonNull(cardType) ? CardType.valueOf(cardType) : null)
                    .gatewayAuthCode(response.getApprovalCode())
                    .gatewayResponseCode(returnCode)
                    .transactionStatus((returnCode.equals(Approved.name())) ? SUCCESS_MESSAGE : FAILURE_MESSAGE);
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
        CurrencyConversion cc = Objects.nonNull(currencyConversion) ? currencyConversion : new CurrencyConversion();
        Card card = transactionDetails.getCard();
        Payment.PaymentBuilder newPayment = Payment.builder();
        String randomId = UUID.randomUUID().toString();
        DetailedAmount detailedAmount = transactionAmount.getDetailedAmount();
        Address billingAddress = Objects.nonNull(customer.getBillingAddress()) ? customer.getBillingAddress() : new Address();
        CPRequestHeaders headers = request.getHeaders();
        SaleItem<?> saleItem = transactionDetails.getSaleItem();
        String saleType = saleItem.getSaleType();
        newPayment
                .paymentId(randomId)
                .referenceId(null)
                .groupId(null)
                //.gatewayRelationNumber(headers.getCorrelationId())
                .clientReferenceNumber(saleItem.getSaleReferenceIdentifier())
                .gatewayId(Gateway.SHIFT4)
                .amount(detailedAmount.getAmount())
                .clientId(headers.getClientId())
                .orderType(Objects.nonNull(saleType) ? OrderType.valueOf(saleType) : null)
                .mgmId(null)
                .mgmToken(card.getMaskedCardNumber())
                .cardHolderName(card.getCardHolderName())
                .tenderCategory(null)
                .currencyCode(cc.getBinCurrencyCode())
                //.last4DigitsOfCard()
                .billingAddress1(billingAddress.getAddressLine())
                .billingAddress2(billingAddress.getStreetName())
                .billingCity(billingAddress.getTownName())
                .billingState(billingAddress.getTownName())
                .billingZipCode(billingAddress.getPostCode())
                .billingCountry(billingAddress.getCountry())
                .clerkId(transactionDetails.getMerchant().getClerkIdentifier())
                .transactionType(TransactionType.AUTHORIZE)
                //.gatewayReasonCode().gatewayReasonDescription()
                //.gatewayAuthSource()
                .deferredAuth(null)
                .createdTimeStamp(helper.convertToTimestamp(request.getTransactionDateTime()))
                //.createdBy().updatedBy()
                .correlationId(headers.getCorrelationId())
                .journeyId(headers.getJourneyId())
                .transactionSessionId(headers.getTransactionId())
                .cardEntryMode(card.getCardEntryMode())
                //.avsResponseCode().cvvResponseCode().dccFlag().dccControlNumber().dccAmount().dccBinRate().dccBinCurrency()
                //.processorStatusCode().processorStatusMessage().processorAuthCode()
                .authSubType(request.getTransactionType())
                .tenderType(TenderType.valueOf(card.getCardType()))
                .updatedTimestamp(LocalDateTime.now());
        if (Objects.nonNull(response)) {
            String transDate = response.getTransDate();
            String cardType = response.getCardType();
            String returnCode = Objects.nonNull(response.getReturnCode()) ? response.getReturnCode() : "";
            String vendorTranID = response.getVendorTranID();
            newPayment
                    .authChainId(Objects.nonNull(vendorTranID) ? Long.valueOf(vendorTranID) : null)
                    .gatewayChainId(Objects.nonNull(vendorTranID) ? vendorTranID.replaceFirst("^0+(?!$)", "") : null)
                    .authorizedAmount(response.getTotalAuthAmount())
                    .issuerType(Objects.nonNull(cardType) ? CardType.valueOf(cardType) : null)
                    .gatewayAuthCode(response.getApprovalCode())
                    .gatewayResponseCode(returnCode)
                    .transactionStatus((returnCode.equals(Approved.name())) ? SUCCESS_MESSAGE : FAILURE_MESSAGE);
        }
        Payment payment = newPayment.build();
        logger.log(Level.INFO, "Transaction Type is: " + payment.getTransactionType());
        return  this.paymentRepository.save(payment);
    }

    @Override
    public Payment saveCaptureAuthPayment(CPPaymentCaptureRequest request, CaptureRouterResponse response, Payment initialPayment) {
        TransactionDetails transactionDetails = request.getTransactionDetails();
        TransactionAmount transactionAmount = transactionDetails.getTransactionAmount();
        Customer customer = transactionDetails.getCustomer();
        CurrencyConversion currencyConversion = transactionDetails.getCurrencyConversion();
        CurrencyConversion cc = Objects.nonNull(currencyConversion) ? currencyConversion : new CurrencyConversion();
        Card card = transactionDetails.getCard();
        Payment.PaymentBuilder newPayment = Payment.builder();
        String string = UUID.randomUUID().toString();
        DetailedAmount detailedAmount = transactionAmount.getDetailedAmount();
        Address billingAddress = Objects.nonNull(customer.getBillingAddress()) ? customer.getBillingAddress() : new Address();
        CPRequestHeaders headers = request.getHeaders();
        String authChainId = request.getAuthChainId();
        Gateway gatewayId = (Objects.nonNull(initialPayment) && Objects.nonNull(initialPayment.getGatewayId())) ? initialPayment.getGatewayId() : null;
        SaleItem<?> saleItem = transactionDetails.getSaleItem();
        String saleType = saleItem.getSaleType();
        newPayment
                .paymentId(string)
                .referenceId(String.valueOf(request.getReferenceId()))
                .groupId(null)
                .gatewayId(gatewayId)
                //.gatewayRelationNumber(headers.getCorrelationId())
                .clientReferenceNumber(saleItem.getSaleReferenceIdentifier())
                .amount(detailedAmount.getAmount())
                .authChainId(Objects.nonNull(authChainId) ? Long.valueOf(authChainId) : null)
                .gatewayChainId(Objects.nonNull(authChainId) ? authChainId.replaceFirst("^0+(?!$)", "") : null)
                .clientId(headers.getClientId())
                .orderType(Objects.nonNull(saleType) ? OrderType.valueOf(saleType) : null)
                .mgmId(null)
                .mgmToken(card.getMaskedCardNumber())
                .cardHolderName(card.getCardHolderName())
                .tenderCategory(null)
                .currencyCode(cc.getBinCurrencyCode())
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
                .createdTimeStamp(helper.convertToTimestamp(request.getTransactionDateTime()))
                //.createdBy().updatedBy()
                .correlationId(headers.getCorrelationId())
                .journeyId(headers.getJourneyId())
                .transactionSessionId(headers.getTransactionId())
                .cardEntryMode(card.getCardEntryMode())
                //.avsResponseCode().cvvResponseCode().dccFlag().dccControlNumber().dccAmount().dccBinRate().dccBinCurrency()
                //.processorStatusCode().processorStatusMessage().processorAuthCode()
                .authSubType(request.getTransactionType())
                .tenderType(TenderType.valueOf(card.getCardType()))
                .updatedTimestamp(LocalDateTime.now());
        if (Objects.nonNull(response)) {
            String transDate = response.getTransDate();
            CardType cardType = response.getCardType();
            String returnCode = Objects.nonNull(response.getReturnCode()) ? response.getReturnCode() : "";
            newPayment
                    .authorizedAmount(response.getTotalAuthAmount())
                    .issuerType(Objects.nonNull(cardType) ? cardType : null)
                    .gatewayAuthCode(response.getApprovalCode())
                    .gatewayResponseCode(returnCode)
                    .transactionStatus((returnCode.equals(Approved.name())) ? SUCCESS_MESSAGE : FAILURE_MESSAGE);
        }
        Payment payment = newPayment.build();
        logger.log(Level.INFO, "Transaction Type is: " + payment.getTransactionType());
        return this.paymentRepository.save(payment);
    }

    @Override
    public Payment saveCardVoidAuthPayment(CPPaymentCardVoidRequest request, CardVoidRouterResponse response, Payment initialPayment) {
        BaseTransactionDetails transactionDetails = request.getTransactionDetails();
        Card card = new Card();
        if (Objects.nonNull(transactionDetails)) {
            card = transactionDetails.getCard();
        }
        Payment.PaymentBuilder newPayment = Payment.builder();
        String string = UUID.randomUUID().toString();
        CPRequestHeaders headers = request.getHeaders();
        String authChainId = request.getAuthChainId();
        Gateway gatewayId = (Objects.nonNull(initialPayment) && Objects.nonNull(initialPayment.getGatewayId())) ? initialPayment.getGatewayId() : null;
        SaleItem<?> saleItem = transactionDetails.getSaleItem();
        String saleType = saleItem.getSaleType();
        newPayment
                .paymentId(string)
                .referenceId(String.valueOf(request.getReferenceId()))
                .groupId(null)
                .gatewayId(gatewayId)
                //.gatewayRelationNumber(headers.getCorrelationId())
                .gatewayChainId(Objects.nonNull(authChainId) ? authChainId.replaceFirst("^0+(?!$)", "") : null)
                .clientReferenceNumber(saleItem.getSaleReferenceIdentifier())
                //.amount(detailedAmount.getAmount())
                .authChainId(Objects.nonNull(authChainId) ? Long.valueOf(authChainId) : null)
                .clientId(headers.getClientId())
                .orderType(Objects.nonNull(saleType) ? OrderType.valueOf(saleType) : null)
                .mgmId(null)
                .mgmToken(card.getMaskedCardNumber())
                .cardHolderName(card.getCardHolderName())
                .tenderCategory(null)
                //.currencyCode(currencyConversion.getBinCurrencyCode())
                //.last4DigitsOfCard()
                .clerkId(transactionDetails.getMerchant().getClerkIdentifier())
                .transactionType(TransactionType.VOID)
                //.gatewayReasonCode().gatewayReasonDescription().gatewayAuthSource()
                .deferredAuth(null)
                .createdTimeStamp(helper.convertToTimestamp(request.getTransactionDateTime()))
                //.createdBy().updatedBy()
                .correlationId(headers.getCorrelationId())
                .journeyId(headers.getJourneyId())
                .transactionSessionId(headers.getTransactionId())
                .cardEntryMode(card.getCardEntryMode())
                .tenderType(TenderType.valueOf(card.getCardType()))
                .updatedTimestamp(LocalDateTime.now());
                //.avsResponseCode().cvvResponseCode().dccFlag().dccControlNumber().dccAmount().dccBinRate().dccBinCurrency()
                //.processorStatusCode().processorStatusMessage().processorAuthCode()
                //.authSubType(AuthType.valueOf(request.getTransactionType()));
        if (Objects.nonNull(response)) {
            String transDate = response.getTransDate();
            String returnCode = Objects.nonNull(response.getReturnCode()) ? response.getReturnCode() : "";
            newPayment
                    .authorizedAmount(response.getTotalAuthAmount())
                    .issuerType(response.getCardType())
                    .gatewayAuthCode(response.getApprovalCode())
                    .gatewayResponseCode(returnCode)
                    .transactionStatus((returnCode.equals(Approved.name())) ? SUCCESS_MESSAGE : FAILURE_MESSAGE);
        }
        Payment payment = newPayment.build();
        logger.log(Level.INFO, "Transaction Type is: " + payment.getTransactionType());
        return this.paymentRepository.save(payment);
    }

    @Override
    public Payment saveRefundPayment(CPPaymentRefundRequest request, RefundRouterResponse response, Payment initialPayment) {
        Payment.PaymentBuilder newPayment = Payment.builder();
        TransactionDetails transactionDetails = request.getTransactionDetails();
        TransactionAmount transactionAmount = transactionDetails.getTransactionAmount();
        CurrencyConversion currencyConversion = transactionDetails.getCurrencyConversion();
        CurrencyConversion cc = Objects.nonNull(currencyConversion) ? currencyConversion : new CurrencyConversion();
        Card card = transactionDetails.getCard();
        Customer customer = transactionDetails.getCustomer();
        String string = UUID.randomUUID().toString();
        DetailedAmount detailedAmount = transactionAmount.getDetailedAmount();
        Address billingAddress = Objects.nonNull(customer.getBillingAddress()) ? customer.getBillingAddress() : new Address();
        CPRequestHeaders headers = request.getHeaders();
        String authChainId = request.getAuthChainId();
        Gateway gatewayId = (Objects.nonNull(initialPayment) && Objects.nonNull(initialPayment.getGatewayId())) ? initialPayment.getGatewayId() : null;
        SaleItem<?> saleItem = transactionDetails.getSaleItem();
        newPayment
                .paymentId(string)
                .referenceId(String.valueOf(request.getReferenceId()))
                .groupId(null)
                //.gatewayRelationNumber(headers.getCorrelationId())
                .gatewayChainId(Objects.nonNull(authChainId) ? authChainId.replaceFirst("^0+(?!$)", "") : null)
                .clientReferenceNumber(saleItem.getSaleReferenceIdentifier())
                .amount(detailedAmount.getAmount())
                .authChainId(Objects.nonNull(authChainId) ? Long.valueOf(authChainId) : null)
                .gatewayId(gatewayId)
                .clientId(headers.getClientId())
                .orderType(OrderType.valueOf(saleItem.getSaleType()))
                .mgmId(null)
                .mgmToken(card.getMaskedCardNumber())
                .cardHolderName(card.getCardHolderName())
                .tenderCategory(null)
                .currencyCode(cc.getBinCurrencyCode())
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
                .createdTimeStamp(helper.convertToTimestamp(request.getTransactionDateTime()))
                //.createdBy().updatedBy()
                .correlationId(headers.getCorrelationId())
                .journeyId(headers.getJourneyId())
                .transactionSessionId(headers.getTransactionId())
                .cardEntryMode(card.getCardEntryMode())
                //.avsResponseCode().cvvResponseCode().dccFlag().dccControlNumber().dccAmount().dccBinRate().dccBinCurrency()
                //.processorStatusCode().processorStatusMessage().processorAuthCode()
                .authSubType(request.getTransactionType())
                .tenderType(TenderType.valueOf(card.getCardType()))
                .updatedTimestamp(LocalDateTime.now());
        if (Objects.nonNull(response)) {
            String transDate = response.getTransDate();
            String returnCode = Objects.nonNull(response.getReturnCode()) ? response.getReturnCode() : "";
            newPayment
                    .authorizedAmount(response.getTotalAuthAmount())
                    .issuerType(response.getCardType())
                    .gatewayAuthCode(response.getApprovalCode())
                    .gatewayResponseCode(returnCode)
                    .transactionStatus((returnCode.equals(Approved.name())) ? SUCCESS_MESSAGE : FAILURE_MESSAGE);
        }
        Payment payment = newPayment.build();
        logger.log(Level.INFO, "Transaction Type is: " + payment.getTransactionType());
        return this.paymentRepository.save(payment);
    }
}
