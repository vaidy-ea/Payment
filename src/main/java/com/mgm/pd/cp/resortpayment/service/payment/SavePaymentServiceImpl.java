package com.mgm.pd.cp.resortpayment.service.payment;

import com.mgm.pd.cp.payment.common.constant.AuthType;
import com.mgm.pd.cp.payment.common.constant.TransactionType;
import com.mgm.pd.cp.payment.common.dto.opera.Card;
import com.mgm.pd.cp.payment.common.dto.opera.TransactionAmount;
import com.mgm.pd.cp.resortpayment.dto.CurrencyConversion;
import com.mgm.pd.cp.resortpayment.dto.Customer;
import com.mgm.pd.cp.resortpayment.dto.Merchant;
import com.mgm.pd.cp.resortpayment.dto.TransactionDetails;
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
import com.mgm.pd.cp.resortpayment.model.Payment;
import com.mgm.pd.cp.resortpayment.repository.PaymentRepository;
import com.mgm.pd.cp.resortpayment.util.common.PaymentProcessingServiceHelper;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.Objects;

import static com.mgm.pd.cp.payment.common.constant.ApplicationConstants.SUCCESS_MESSAGE;
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
                .propertyCode(helper.getValueByName(source, "propertyIdentifier"))
                .merchantId(merchant.getMerchantIdentifier())
                .originDate(helper.getValueByName(source, "originDate"))
                .resvNameID(transactionDetails.getSaleItem().getSaleReferenceIdentifier())
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
    public Payment saveAuthorizationPayment(CPPaymentAuthorizationRequest cpPaymentAuthorizationRequest, AuthorizationRouterResponse intelligentRouterResponse) {
        Payment.PaymentBuilder newPayment = Payment.builder();
        newPayment.propertyCode(cpPaymentAuthorizationRequest.getPropertyCode())
                .sequenceNumber(cpPaymentAuthorizationRequest.getSequenceNumber())
                .resvNameID(cpPaymentAuthorizationRequest.getResvNameID())
                .authType(cpPaymentAuthorizationRequest.getAuthType())
                .usageType(cpPaymentAuthorizationRequest.getUsageType())
                .guestName(cpPaymentAuthorizationRequest.getGuestName())
                .transDate(cpPaymentAuthorizationRequest.getTransDate())
                .originDate(cpPaymentAuthorizationRequest.getOriginDate())
                .binCurrencyCode(cpPaymentAuthorizationRequest.getBinCurrencyCode())
                .currencyIndicator(cpPaymentAuthorizationRequest.getCurrencyIndicator())
                .cardNumber(cpPaymentAuthorizationRequest.getCardNumber())
                .cardExpirationDate(cpPaymentAuthorizationRequest.getCardExpirationDate())
                .balance(cpPaymentAuthorizationRequest.getBalance())
                .trackIndicator(cpPaymentAuthorizationRequest.getTrackIndicator())
                .incrementalAuthInvoiceId(cpPaymentAuthorizationRequest.getIncrementalAuthInvoiceId())
                .cpTransactionType(TransactionType.INIT_AUTH_CNP);
        if (Objects.nonNull(intelligentRouterResponse)) {
            newPayment.binCurrencyCode(intelligentRouterResponse.getBinCurrencyCode())
            .cardExpirationDate(intelligentRouterResponse.getCardExpirationDate())
                    .returnCode(intelligentRouterResponse.getReturnCode())
                    . cardNumber(String.valueOf(intelligentRouterResponse.getCardNumber()))
                    .resvNameID(intelligentRouterResponse.getResvNameID())
            .sequenceNumber(intelligentRouterResponse.getSequenceNumber())
            .transDate(intelligentRouterResponse.getTransDate())
            .approvalCode(intelligentRouterResponse.getApprovalCode())
            .comments(Objects.nonNull(intelligentRouterResponse.getComments()) ? intelligentRouterResponse.getComments() : SUCCESS_MESSAGE);
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
                .propertyCode(helper.getValueByName(captureRequest, "propertyIdentifier"))
                //TODO: check if below fields are coming in IntelligentRouterResponse
                //.usageType(captureRequest.getUsageType())
                .guestName(customer.getFullName())
                .dccAmount(Double.valueOf(currencyConversion.getAmount()))
                .binRate(currencyConversion.getBinCurrencyRate())
                .binCurrencyCode(currencyConversion.getBinCurrencyCode())
                .cardNumber(card.getMaskedCardNumber())
                .cardExpirationDate(card.getExpiryDate())
                .resvNameID(transactionDetails.getSaleItem().getSaleReferenceIdentifier())
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
        Payment.PaymentBuilder newPayment = Payment.builder();
        newPayment.issueNumber(voidRequest.getIssueNumber())
                .startDate(voidRequest.getStartDate())
                .propertyCode(voidRequest.getPropertyCode())
                //TODO: check if below fields are coming in IntelligentRouterResponse
                .usageType(voidRequest.getUsageType())
                .guestName(voidRequest.getGuestName())
                .binRate(voidRequest.getBinRate())
                .dccAmount(voidRequest.getDccAmount())
                .binCurrencyCode(voidRequest.getBinCurrencyCode())
                .resvNameID(voidRequest.getResvNameID())
                .trackIndicator(voidRequest.getTrackIndicator())
                .incrementalAuthInvoiceId(voidRequest.getIncrementalAuthInvoiceId())
                .merchantId(voidRequest.getMerchantID())
                .sequenceNumber(voidRequest.getSequenceNumber())
                .transDate(voidRequest.getTransDate())
                .uniqueID(voidRequest.getUniqueID())
                .clientID(voidRequest.getClientID())
                .corelationId(voidRequest.getCorelationId())
                .cardNumber(voidRequest.getCardNumber())
                .cpTransactionType(TransactionType.CARD_VOID)
                .comments(voidRequest.getComments());
        if (Objects.nonNull(vrResponse)) {
            newPayment.settleAmount(vrResponse.getSettleAmount())
                    .cardType(String.valueOf(vrResponse.getCardType()))
                    .cardExpirationDate(voidRequest.getCardExpirationDate())
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
        newPayment
                .authAmountRequested(transactionAmount.getRequestedAmount())
                .binRate(currencyConversion.getBinCurrencyRate())
                .authTotalAmount(transactionAmount.getAuthorizedAmount())
                .balance(transactionAmount.getDetailedAmount().getAmount())
                .binCurrencyCode(currencyConversion.getBinCurrencyCode())
                .cardNumber(card.getCardHolderName())
                .cardExpirationDate(card.getExpiryDate())
                .currencyIndicator(currencyConversion.getConversionFlag())
                .guestName(customer.getFullName())
                .resvNameID(transactionDetails.getSaleItem().getSaleReferenceIdentifier())
                .merchantId(merchant.getMerchantIdentifier())
                .clientID(cpPaymentRefundRequest.getClientID())
                .corelationId(cpPaymentRefundRequest.getCorelationId())
                .comments(cpPaymentRefundRequest.getComments())
                .cpTransactionType(REFUND);
        if (Objects.nonNull(refundResponse)) {
            newPayment.authTotalAmount(refundResponse.getTotalAuthAmount())
                    .returnCode(String.valueOf(refundResponse.getResponseCode()))
                    .vendorTranID(refundResponse.getVendorTranID())
                    .sequenceNumber(refundResponse.getSequenceNumber())
                    .transDate(refundResponse.getTransDate())
                    .approvalCode(refundResponse.getResponseCode())
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
