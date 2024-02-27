package com.mgm.pd.cp.resortpayment.service.payment;

import com.mgm.pd.cp.payment.common.constant.AuthType;
import com.mgm.pd.cp.payment.common.constant.TransactionType;
import com.mgm.pd.cp.payment.common.dto.opera.Card;
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
import com.mgm.pd.cp.resortpayment.util.common.PaymentProcessingServiceHelper;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.Objects;

import static com.mgm.pd.cp.payment.common.constant.ApplicationConstants.*;
import static com.mgm.pd.cp.payment.common.constant.TransactionType.REFUND;

@Service
@AllArgsConstructor
public class SavePaymentServiceImpl implements SavePaymentService {
    private static final Logger logger = LogManager.getLogger(SavePaymentServiceImpl.class);

    private PaymentRepository paymentRepository;
    private PaymentProcessingServiceHelper helper;

    @Override
    public Payment saveIncrementalAuthorizationPayment(CPPaymentIncrementalAuthRequest source, IncrementalAuthorizationRouterResponse irResponse) {
        TransactionDetails transactionDetails = source.getTransactionDetails();
        TransactionAmount transactionAmount = transactionDetails.getTransactionAmount();
        Customer customer = transactionDetails.getCustomer();
        CurrencyConversion currencyConversion = transactionDetails.getCurrencyConversion();
        Card card = transactionDetails.getCard();
        Merchant merchant = transactionDetails.getMerchant();
        Payment.PaymentBuilder newPayment = Payment.builder();
        SaleItem saleItem = transactionDetails.getSaleItem();
        newPayment
                .authAmountRequested(transactionAmount.getRequestedAmount())
                .currencyIndicator(transactionAmount.getCurrencyIndicator())
                .guestName(customer.getFullName())
                .dccAmount(Double.valueOf(currencyConversion.getAmount()))
                .binRate(currencyConversion.getBinCurrencyRate())
                .uniqueID(card.getTokenValue())
                .binCurrencyCode(currencyConversion.getBinCurrencyCode())
                .cardNumber(card.getMaskedCardNumber())
                .cardExpirationDate(card.getExpiryDate())
                .cardType(String.valueOf(card.getCardType()))
                .startDate(card.getStartDate())
                .issueNumber(Integer.valueOf(card.getSequenceNumber()))
                .propertyCode(helper.getValueFromSaleDetails(source, PROPERTY_IDENTIFIER))
                .merchantId(merchant.getMerchantIdentifier())
                .originDate(helper.getValueFromSaleDetails(source, ORIGIN_DATE))
                .resvNameID(Objects.nonNull(saleItem) ? saleItem.getSaleReferenceIdentifier() : null)
                .vendorTranID(source.getGatewayInfo().getGatewayTransactionIdentifier())
                .balance(transactionDetails.getTransactionAmount().getBalanceAmount())
                .sequenceNumber(source.getTransactionIdentifier())
                .transDate(source.getTransactionDateTime())
                .authType(AuthType.valueOf(source.getTransactionType()))
                .clientID(source.getClientID())
                .corelationId(source.getCorelationId())
                .incrementalAuthInvoiceId(source.getIncrementalAuthInvoiceId())
                //.usageType(incrementalRequest.getUsageType())
                //.trackIndicator(incrementalRequest.getTrackIndicator())
                .cpTransactionType(TransactionType.INCREMENTAL_AUTH)
                .comments(source.getComments());
        if (Objects.nonNull(irResponse)) {
            newPayment
                    .authTotalAmount(irResponse.getTotalAuthAmount())
                    .cardType(irResponse.getCardType())
                    .returnCode(irResponse.getReturnCode())
                    .sequenceNumber(irResponse.getSequenceNumber())
                    .transDate(irResponse.getTransDate())
                    .vendorTranID(irResponse.getVendorTranID())
                    .approvalCode(irResponse.getApprovalCode())
                    .comments(Objects.nonNull(irResponse.getComments()) ? irResponse.getComments() : SUCCESS_MESSAGE);
        }
        Payment payment = newPayment.build();
        logger.log(Level.INFO, "Transaction Type is: " + payment.getCpTransactionType());
        return this.paymentRepository.save(payment);
    }

    @Override
    public Payment saveAuthorizationPayment(CPPaymentAuthorizationRequest source, AuthorizationRouterResponse authorizationRouterResponse) {
        TransactionDetails transactionDetails = source.getTransactionDetails();
        TransactionAmount transactionAmount = transactionDetails.getTransactionAmount();
        Customer customer = transactionDetails.getCustomer();
        CurrencyConversion currencyConversion = transactionDetails.getCurrencyConversion();
        Card card = transactionDetails.getCard();
        Payment.PaymentBuilder newPayment = Payment.builder();
        newPayment.propertyCode(helper.getValueFromSaleDetails(source, PROPERTY_IDENTIFIER))
                .authType(AuthType.valueOf(source.getTransactionType()))
                //.usageType(incrementalRequest.getUsageType())
                .guestName(customer.getFullName())
                .transDate(source.getTransactionDateTime())
                .originDate(helper.getValueFromSaleDetails(source, ORIGIN_DATE))
                .binCurrencyCode(currencyConversion.getBinCurrencyCode())
                .currencyIndicator(transactionAmount.getCurrencyIndicator())
                .cardNumber(card.getMaskedCardNumber())
                .cardExpirationDate(card.getExpiryDate())
                .balance(transactionDetails.getTransactionAmount().getBalanceAmount())
                //.trackIndicator(incrementalRequest.getTrackIndicator())
                .incrementalAuthInvoiceId(source.getIncrementalAuthInvoiceId())
                .cpTransactionType(TransactionType.INIT_AUTH_CNP);
        if (Objects.nonNull(authorizationRouterResponse)) {
            SaleItem saleItem = transactionDetails.getSaleItem();
            newPayment.binCurrencyCode(authorizationRouterResponse.getBinCurrencyCode())
            .cardExpirationDate(authorizationRouterResponse.getCardExpirationDate())
            .returnCode(authorizationRouterResponse.getReturnCode())
            .cardNumber(String.valueOf(authorizationRouterResponse.getCardNumber()))
            .resvNameID(Objects.nonNull(saleItem) ? saleItem.getSaleReferenceIdentifier() : null)
            .sequenceNumber(authorizationRouterResponse.getSequenceNumber())
            .transDate(authorizationRouterResponse.getTransDate())
            .approvalCode(authorizationRouterResponse.getApprovalCode())
            .comments(Objects.nonNull(authorizationRouterResponse.getComments()) ? authorizationRouterResponse.getComments() : SUCCESS_MESSAGE);
        }
        Payment payment = newPayment.build();
        logger.log(Level.INFO, "Transaction Type is: " + payment.getCpTransactionType());
        return  this.paymentRepository.save(payment);
    }

    @Override
    public Payment saveCaptureAuthPayment(CPPaymentCaptureRequest captureRequest
            , CaptureRouterResponse crResponse, Double initialAuthAmount) {
        TransactionDetails transactionDetails = captureRequest.getTransactionDetails();
        TransactionAmount transactionAmount = transactionDetails.getTransactionAmount();
        Customer customer = transactionDetails.getCustomer();
        CurrencyConversion currencyConversion = transactionDetails.getCurrencyConversion();
        Card card = transactionDetails.getCard();
        Merchant merchant = transactionDetails.getMerchant();
        Payment.PaymentBuilder newPayment = Payment.builder();
        newPayment
                .merchantId(merchant.getMerchantIdentifier())
                .issueNumber(Integer.valueOf(card.getSequenceNumber()))
                .startDate(card.getStartDate())
                .uniqueID(card.getTokenValue())
                .clientID(captureRequest.getClientID())
                .corelationId(captureRequest.getCorelationId())
                .propertyCode(helper.getValueFromSaleDetails(captureRequest, PROPERTY_IDENTIFIER))
                //TODO: check if below fields are coming in IntelligentRouterResponse
                //.usageType(captureRequest.getUsageType())
                .guestName(customer.getFullName())
                .dccAmount(Double.valueOf(currencyConversion.getAmount()))
                .binRate(currencyConversion.getBinCurrencyRate())
                .binCurrencyCode(currencyConversion.getBinCurrencyCode())
                .cardNumber(card.getMaskedCardNumber())
                .cardExpirationDate(card.getExpiryDate())
                .resvNameID(Objects.nonNull(transactionDetails.getSaleItem()) ? transactionDetails.getSaleItem().getSaleReferenceIdentifier() : null)
                //.trackIndicator(captureRequest.getTrackIndicator())
                .incrementalAuthInvoiceId(captureRequest.getIncrementalAuthInvoiceId())
                .sequenceNumber(captureRequest.getTransactionIdentifier())
                .transDate(captureRequest.getTransactionDateTime())
                .settleAmount(transactionAmount.getCumulativeAmount())
                .cpTransactionType(setCaptureTransactionType(initialAuthAmount, transactionAmount.getCumulativeAmount()))
                .comments(captureRequest.getComments());
        if (Objects.nonNull(crResponse)) {
            newPayment.authTotalAmount(crResponse.getTotalAuthAmount())
                    .returnCode(String.valueOf(crResponse.getReturnCode()))
                    .vendorTranID(crResponse.getVendorTranID())
                    .approvalCode(crResponse.getApprovalCode())
                    .cardType(String.valueOf(crResponse.getCardType()))
                    .comments(Objects.nonNull(crResponse.getComments()) ? crResponse.getComments() : SUCCESS_MESSAGE);
        }
        Payment payment = newPayment.build();
        logger.log(Level.INFO, "Transaction Type is: " + payment.getCpTransactionType());
        return this.paymentRepository.save(payment);
    }

    @Override
    public Payment saveCardVoidAuthPayment(CPPaymentCardVoidRequest voidRequest, CardVoidRouterResponse vrResponse) {
        BaseTransactionDetails transactionDetails = voidRequest.getTransactionDetails();
        Card card = new Card();
        Merchant merchant = new Merchant();
        SaleItem<?> saleItem = new SaleItem<>();
        if (Objects.nonNull(transactionDetails)) {
            card = transactionDetails.getCard();
            merchant = transactionDetails.getMerchant();
            saleItem = transactionDetails.getSaleItem();
        }
        Payment.PaymentBuilder newPayment = Payment.builder();
        String sequenceNumber = card.getSequenceNumber();
        newPayment.issueNumber(Objects.nonNull(sequenceNumber) ? Integer.valueOf(sequenceNumber) : null)
                .startDate(card.getStartDate())
                .propertyCode(helper.getValueFromSaleDetails(voidRequest, PROPERTY_IDENTIFIER))
                //TODO: check if below fields are coming in IntelligentRouterResponse
                /*.usageType(captureRequest.getUsageType())
                .guestName(customer.getFullName())
                .dccAmount(Double.valueOf(currencyConversion.getAmount()))
                .binRate(currencyConversion.getBinCurrencyRate())
                .binCurrencyCode(currencyConversion.getBinCurrencyCode())*/
                .resvNameID(Objects.nonNull(saleItem) ? saleItem.getSaleReferenceIdentifier() : null)
                //.trackIndicator(captureRequest.getTrackIndicator()).incrementalAuthInvoiceId(voidRequest.getIncrementalAuthInvoiceId())
                .merchantId(merchant.getMerchantIdentifier())
                .sequenceNumber(voidRequest.getTransactionIdentifier())
                .transDate(voidRequest.getTransactionDateTime())
                .uniqueID(card.getTokenValue())
                .clientID(voidRequest.getClientID())
                .corelationId(voidRequest.getCorelationId())
                .cardNumber(card.getMaskedCardNumber())
                .cardExpirationDate(card.getExpiryDate())
                .incrementalAuthInvoiceId(voidRequest.getIncrementalAuthInvoiceId())
                //.settleAmount(transactionAmount.getCumulativeAmount())
                .cpTransactionType(TransactionType.CARD_VOID)
                .comments(voidRequest.getComments());
        if (Objects.nonNull(vrResponse)) {
            newPayment.cardType(String.valueOf(vrResponse.getCardType()))
                    .returnCode(String.valueOf(vrResponse.getReturnCode()))
                    .vendorTranID(vrResponse.getVendorTranID())
                    .authTotalAmount(vrResponse.getTotalAuthAmount())
                    .approvalCode(vrResponse.getApprovalCode())
                    .comments(Objects.nonNull(vrResponse.getComments()) ? vrResponse.getComments() : SUCCESS_MESSAGE);
        }
        Payment payment = newPayment.build();
        logger.log(Level.INFO, "Transaction Type is: " +  payment.getCpTransactionType());
        return this.paymentRepository.save(payment);
    }

    @Override
    public Payment saveRefundPayment(CPPaymentRefundRequest cpPaymentRefundRequest, RefundRouterResponse refundResponse) {
        Payment.PaymentBuilder newPayment = Payment.builder();
        TransactionDetails transactionDetails = cpPaymentRefundRequest.getTransactionDetails();
        TransactionAmount transactionAmount = transactionDetails.getTransactionAmount();
        CurrencyConversion currencyConversion = transactionDetails.getCurrencyConversion();
        Card card = transactionDetails.getCard();
        Customer customer = transactionDetails.getCustomer();
        Merchant merchant = transactionDetails.getMerchant();
        String sequenceNumber = card.getSequenceNumber();
        newPayment
                .authAmountRequested(transactionAmount.getRequestedAmount())
                .binRate(currencyConversion.getBinCurrencyRate())
                .authTotalAmount(transactionAmount.getAuthorizedAmount())
                .balance(transactionAmount.getDetailedAmount().getAmount())
                .binCurrencyCode(currencyConversion.getBinCurrencyCode())
                .cardNumber(card.getMaskedCardNumber())
                .cardExpirationDate(card.getExpiryDate())
                .propertyCode(helper.getValueFromSaleDetails(cpPaymentRefundRequest, PROPERTY_IDENTIFIER))
                .issueNumber(Objects.nonNull(sequenceNumber) ? Integer.valueOf(sequenceNumber) : null)
                //.currencyIndicator(currencyConversion.getConversionFlag())
                .guestName(customer.getFullName())
                .resvNameID(Objects.nonNull(transactionDetails.getSaleItem()) ? transactionDetails.getSaleItem().getSaleReferenceIdentifier() : null)
                .merchantId(merchant.getMerchantIdentifier())
                .clientID(cpPaymentRefundRequest.getClientID())
                .corelationId(cpPaymentRefundRequest.getCorelationId())
                .comments(cpPaymentRefundRequest.getComments())
                .incrementalAuthInvoiceId(cpPaymentRefundRequest.getIncrementalAuthInvoiceId())
                .sequenceNumber(cpPaymentRefundRequest.getTransactionIdentifier())
                .cpTransactionType(REFUND);
        if (Objects.nonNull(refundResponse)) {
            newPayment.authTotalAmount(refundResponse.getTotalAuthAmount())
                    .returnCode(refundResponse.getReturnCode())
                    .vendorTranID(refundResponse.getVendorTranID())
                    .sequenceNumber(refundResponse.getSequenceNumber())
                    .transDate(refundResponse.getTransDate())
                    .approvalCode(refundResponse.getApprovalCode())
                    .cardType(String.valueOf(refundResponse.getCardType()))
                    .comments(Objects.nonNull(refundResponse.getComments()) ? refundResponse.getComments() : SUCCESS_MESSAGE);
        }
        Payment payment = newPayment.build();
        logger.log(Level.INFO, "Transaction Type is: " + payment.getCpTransactionType());
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
