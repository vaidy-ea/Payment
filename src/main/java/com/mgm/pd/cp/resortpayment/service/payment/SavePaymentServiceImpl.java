package com.mgm.pd.cp.resortpayment.service.payment;

import com.mgm.pd.cp.resortpayment.constant.ApplicationConstants;
import com.mgm.pd.cp.resortpayment.constant.TransactionType;
import com.mgm.pd.cp.resortpayment.dto.capture.CPPaymentCaptureRequest;
import com.mgm.pd.cp.resortpayment.dto.capture.CaptureRouterResponse;
import com.mgm.pd.cp.resortpayment.dto.cardvoid.CPPaymentCardVoidRequest;
import com.mgm.pd.cp.resortpayment.dto.cardvoid.CardVoidRouterResponse;
import com.mgm.pd.cp.resortpayment.dto.incrementalauth.CPPaymentIncrementalRequest;
import com.mgm.pd.cp.resortpayment.dto.incrementalauth.IncrementalAuthorizationRouterResponse;
import com.mgm.pd.cp.resortpayment.model.Payment;
import com.mgm.pd.cp.resortpayment.repository.PaymentRepository;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@AllArgsConstructor
public class SavePaymentServiceImpl implements SavePaymentService {
    private static final Logger logger = LogManager.getLogger(SavePaymentServiceImpl.class);

    private PaymentRepository paymentRepository;

    @Override
    public Payment saveIncrementalAuthorizationPayment(CPPaymentIncrementalRequest incrementalRequest, IncrementalAuthorizationRouterResponse irResponse) {
        Payment.PaymentBuilder newPayment = Payment.builder();
        newPayment
                .authAmountRequested(incrementalRequest.getAuthorizationAmount())
                .propertyCode(incrementalRequest.getPropertyCode())
                //TODO: check if below fields are coming in IntelligentRouterResponse
                .authType(incrementalRequest.getAuthType())
                .usageType(incrementalRequest.getUsageType())
                .guestName(incrementalRequest.getGuestName())
                .binRate(incrementalRequest.getBinRate())
                .dccAmount(incrementalRequest.getDCCAmount())
                .binCurrencyCode(incrementalRequest.getBinCurrencyCode())
                .cardExpirationDate(incrementalRequest.getCardExpirationDate())
                .cardNumber(incrementalRequest.getCardNumber())
                .resvNameID(incrementalRequest.getResvNameID())
                .authAmountRequested(incrementalRequest.getAuthorizationAmount())
                .currencyIndicator(incrementalRequest.getCurrencyIndicator())
                .balance(incrementalRequest.getBalance())
                .trackIndicator(incrementalRequest.getTrackIndicator())
                .incrementalAuthInvoiceId(incrementalRequest.getIncrementalAuthInvoiceId())
                .originDate(incrementalRequest.getOriginDate())
                .issueNumber(incrementalRequest.getIssueNumber())
                .startDate(incrementalRequest.getStartDate())
                .uniqueID(incrementalRequest.getUniqueID())
                .clientID(incrementalRequest.getClientID())
                .corelationId(incrementalRequest.getCorelationId())
                .cpTransactionType(TransactionType.INCREMENTAL_AUTH);
        if (Objects.nonNull(irResponse)) {
            newPayment
                    .authTotalAmount(irResponse.getTotalAuthAmount())
                    .cardType(irResponse.getCardType())
                    .returnCode(irResponse.getReturnCode())
                    .sequenceNumber(irResponse.getSequenceNumber())
                    .transDate(irResponse.getTransDate())
                    .vendorTranID(irResponse.getVendorTranID())
                    .approvalCode(irResponse.getApprovalCode())
                    .comments(Objects.nonNull(irResponse.getComments()) ? irResponse.getComments() : ApplicationConstants.SUCCESS_MESSAGE);
        }
        Payment payment = newPayment.build();
        logger.log(Level.INFO, "Transaction Type is: " + payment.getCpTransactionType());
        return this.paymentRepository.save(payment);
    }

    @Override
    public Payment saveCaptureAuthPayment(CPPaymentCaptureRequest captureRequest
            , CaptureRouterResponse crResponse, Double initialAuthAmount) {
        Payment.PaymentBuilder newPayment = Payment.builder();
        newPayment
                .merchantId(captureRequest.getMerchantID())
                .issueNumber(captureRequest.getIssueNumber())
                .startDate(captureRequest.getStartDate())
                .uniqueID(captureRequest.getUniqueID())
                .clientID(captureRequest.getClientID())
                .corelationId(captureRequest.getCorelationId())
                .propertyCode(captureRequest.getPropertyCode())
                //TODO: check if below fields are coming in IntelligentRouterResponse
                .usageType(captureRequest.getUsageType())
                .guestName(captureRequest.getGuestName())
                .binRate(captureRequest.getBinRate())
                .dccAmount(captureRequest.getDccAmount())
                .binCurrencyCode(captureRequest.getBinCurrencyCode())
                .cardExpirationDate(captureRequest.getCardExpirationDate())
                .cardNumber(captureRequest.getCardNumber())
                .resvNameID(captureRequest.getResvNameID())
                .trackIndicator(captureRequest.getTrackIndicator())
                .incrementalAuthInvoiceId(captureRequest.getIncrementalAuthInvoiceId())
                .sequenceNumber(captureRequest.getSequenceNumber())
                .transDate(captureRequest.getTransDate())
                .settleAmount(captureRequest.getTotalAuthAmount())
                .cpTransactionType(setCaptureTransactionType(initialAuthAmount, captureRequest.getTotalAuthAmount()));
        if (Objects.nonNull(crResponse)) {
            newPayment.authTotalAmount(crResponse.getTotalAuthAmount())
                    .returnCode(String.valueOf(crResponse.getReturnCode()))
                    .vendorTranID(crResponse.getVendorTranID())
                    .approvalCode(crResponse.getApprovalCode())
                    .cardType(String.valueOf(crResponse.getCardType()))
                    .comments(Objects.nonNull(crResponse.getComments()) ? crResponse.getComments() : ApplicationConstants.SUCCESS_MESSAGE);
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
                .cpTransactionType(TransactionType.CARD_VOID);
        if (Objects.nonNull(vrResponse)) {
            newPayment.settleAmount(vrResponse.getSettleAmount())
                    .cardType(String.valueOf(vrResponse.getCardType()))
                    .cardExpirationDate(voidRequest.getCardExpirationDate())
                    .returnCode(String.valueOf(vrResponse.getReturnCode()))
                    .vendorTranID(vrResponse.getVendorTranID())
                    .authTotalAmount(vrResponse.getTotalAuthAmount())
                    .approvalCode(vrResponse.getApprovalCode())
                    .comments(Objects.nonNull(vrResponse.getComments()) ? vrResponse.getComments() : ApplicationConstants.SUCCESS_MESSAGE);
        }
        Payment payment = newPayment.build();
        logger.log(Level.INFO, "Transaction Type is: " +  payment.getCpTransactionType());
        return this.paymentRepository.save(payment);
    }

    private TransactionType setCaptureTransactionType(Double initialAuthAmount, Double totalAuthAmount) {
        TransactionType captureTransactionType = null;
        if(totalAuthAmount > initialAuthAmount) {
            captureTransactionType =  TransactionType.CAPTURE_ADDITIONAL_AUTH;
        }
        if (totalAuthAmount < initialAuthAmount) {
            captureTransactionType = TransactionType.CAPTURE_PARTIAL_VOID;
        }
        return captureTransactionType;
    }

}
