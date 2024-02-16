package com.mgm.pd.cp.resortpayment.service;

import com.mgm.pd.cp.resortpayment.constant.TransactionType;
import com.mgm.pd.cp.resortpayment.dto.capture.CPPaymentCaptureRequest;
import com.mgm.pd.cp.resortpayment.dto.capture.CaptureRouterResponse;
import com.mgm.pd.cp.resortpayment.dto.cardvoid.CPPaymentVoidRequest;
import com.mgm.pd.cp.resortpayment.dto.cardvoid.VoidRouterResponse;
import com.mgm.pd.cp.resortpayment.dto.incrementalauth.CPPaymentIncrementalRequest;
import com.mgm.pd.cp.resortpayment.dto.incrementalauth.IncrementalRouterResponse;
import com.mgm.pd.cp.resortpayment.model.Payment;
import com.mgm.pd.cp.resortpayment.repository.PaymentRepository;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class SavePaymentServiceImpl implements SavePaymentService {
    private static final Logger logger = LogManager.getLogger(SavePaymentServiceImpl.class);

    private PaymentRepository paymentRepository;

    @Override
    public Payment saveIncrementalAuthPayment(CPPaymentIncrementalRequest incrementalRequest, IncrementalRouterResponse irResponse) {
        Payment newPayment = Payment.builder()
                .authAmountRequested(irResponse.getAuthAmountRequested())
                .authTotalAmount(incrementalRequest.getTotalAuthAmount())
                .cardType(irResponse.getCardType())
                .returnCode(irResponse.getReturnCode())
                .sequenceNumber(irResponse.getSequenceNumber())
                .transDate(irResponse.getTransDate())
                .vendorTranID(irResponse.getVendorTranID())
                .approvalCode(irResponse.getApprovalCode())
                .cardNumberLast4Digits(irResponse.getCardNumberLast4Digits())
                .issueNumber(irResponse.getIssueNumber())
                .message(irResponse.getMessage())
                .printInfo1(irResponse.getPrintInfo1())
                .printInfo2(irResponse.getPrintInfo2())
                .printInfo3(irResponse.getPrintInfo3())
                .printInfo4(irResponse.getPrintInfo4())
                .printInfo5(irResponse.getPrintInfo5())
                .printInfo6(irResponse.getPrintInfo6())
                .printInfo7(irResponse.getPrintInfo7())
                .printInfo8(irResponse.getPrintInfo8())
                .printInfo9(irResponse.getPrintInfo9())
                .startDate(irResponse.getStartDate())
                .uniqueID(irResponse.getUniqueID())
                .clientID(irResponse.getClientID())
                .corelationId(irResponse.getCorelationId())
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
                .cpTransactionType(TransactionType.INCREMENTAL_AUTH)
                .build();
        logger.log(Level.INFO, "Transaction Type is: " + newPayment.getCpTransactionType());
        return this.paymentRepository.save(newPayment);
    }

    @Override
    public Payment saveCaptureAuthPayment(CPPaymentCaptureRequest captureRequest
            , CaptureRouterResponse crResponse, Double initialAuthAmount) {
        Payment newPayment = Payment.builder()
                .authTotalAmount(captureRequest.getTotalAuthAmount())
                .settleAmount(crResponse.getSettleAmount())
                .merchantId(captureRequest.getMerchantID())
                .transReference(crResponse.getTransReference())
                .cardType(String.valueOf(crResponse.getCardType()))
                .returnCode(String.valueOf(crResponse.getReturnCode()))
                .sequenceNumber(crResponse.getSequenceNumber())
                .transDate(crResponse.getTransDate())
                .vendorTranID(crResponse.getVendorTranID())
                .approvalCode(captureRequest.getApprovalCode())
                .issueNumber(captureRequest.getIssueNumber())
                .message(crResponse.getMessage())
                .printInfo1(crResponse.getPrintInfo1())
                .printInfo2(crResponse.getPrintInfo2())
                .printInfo3(crResponse.getPrintInfo3())
                .printInfo4(crResponse.getPrintInfo4())
                .printInfo5(crResponse.getPrintInfo5())
                .printInfo6(crResponse.getPrintInfo6())
                .printInfo7(crResponse.getPrintInfo7())
                .printInfo8(crResponse.getPrintInfo8())
                .printInfo9(crResponse.getPrintInfo9())
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
                .cpTransactionType(setCaptureTransactionType(initialAuthAmount, captureRequest.getTotalAuthAmount()))
                .build();
        logger.log(Level.INFO, "Transaction Type is: " + newPayment.getCpTransactionType());
        return this.paymentRepository.save(newPayment);
    }

    @Override
    public Payment saveVoidAuthPayment(CPPaymentVoidRequest voidRequest, VoidRouterResponse vrResponse) {
        Payment newPayment = Payment.builder()
                .authTotalAmount(voidRequest.getTotalAuthAmount())
                .settleAmount(vrResponse.getSettleAmount())
                .merchantId(vrResponse.getMerchantID())
                .transReference(vrResponse.getTransReference())
                .cardType(String.valueOf(vrResponse.getCardType()))
                .returnCode(String.valueOf(vrResponse.getReturnCode()))
                .sequenceNumber(vrResponse.getSequenceNumber())
                .transDate(vrResponse.getTransDate())
                .vendorTranID(vrResponse.getVendorTranID())
                .approvalCode(voidRequest.getApprovalCode())
                .issueNumber(voidRequest.getIssueNumber())
                .message(vrResponse.getMessage())
                .printInfo1(vrResponse.getPrintInfo1())
                .printInfo2(vrResponse.getPrintInfo2())
                .printInfo3(vrResponse.getPrintInfo3())
                .printInfo4(vrResponse.getPrintInfo4())
                .printInfo5(vrResponse.getPrintInfo5())
                .printInfo6(vrResponse.getPrintInfo6())
                .printInfo7(vrResponse.getPrintInfo7())
                .printInfo8(vrResponse.getPrintInfo8())
                .printInfo9(vrResponse.getPrintInfo9())
                .startDate(voidRequest.getStartDate())
                .uniqueID(vrResponse.getUniqueID())
                .clientID(vrResponse.getClientID())
                .corelationId(vrResponse.getCorelationId())
                .propertyCode(voidRequest.getPropertyCode())
                //TODO: check if below fields are coming in IntelligentRouterResponse
                .usageType(voidRequest.getUsageType())
                .guestName(voidRequest.getGuestName())
                .binRate(voidRequest.getBinRate())
                .dccAmount(voidRequest.getDccAmount())
                .binCurrencyCode(voidRequest.getBinCurrencyCode())
                .cardExpirationDate(voidRequest.getCardExpirationDate())
                .cardNumber(voidRequest.getCardNumber())
                .resvNameID(voidRequest.getResvNameID())
                .trackIndicator(voidRequest.getTrackIndicator())
                .incrementalAuthInvoiceId(voidRequest.getIncrementalAuthInvoiceId())
                .cpTransactionType(TransactionType.VOID)
                .build();
        logger.log(Level.INFO, "Transaction Type is: " + newPayment.getCpTransactionType());
        return this.paymentRepository.save(newPayment);
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
