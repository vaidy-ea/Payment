package com.mgm.pd.cp.resortpayment.util.refund;

import com.mgm.pd.cp.payment.common.constant.TransactionType;
import com.mgm.pd.cp.payment.common.exception.InvalidTransactionAttemptException;
import com.mgm.pd.cp.payment.common.model.Payment;
import com.mgm.pd.cp.resortpayment.dto.refund.CPPaymentRefundRequest;
import com.mgm.pd.cp.resortpayment.util.common.DateHelper;
import lombok.experimental.UtilityClass;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.flywaydb.core.internal.util.Pair;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@UtilityClass
public class RefundValidationHelper {
    private static final Logger logger = LogManager.getLogger(RefundValidationHelper.class);

    public void throwExceptionForInvalidAttempts(Pair<Optional<List<Payment>>, String> optionalInitialAuthPayment, Pair<Optional<Payment>, String> initialPaymentAndApprovalCode) {
        throwExceptionIfApprovalCodeIsNotFound(initialPaymentAndApprovalCode);
        Optional<List<Payment>> optionalPaymentList = optionalInitialAuthPayment.getLeft();
        if(optionalPaymentList.isPresent()) {
            List<Payment> payments = optionalPaymentList.get();
            if (!payments.isEmpty()) {
                throwExceptionIfTransactionAuthChainIdIsAlreadyUsed(optionalInitialAuthPayment, payments);
            }
        }
    }

    private void throwExceptionIfApprovalCodeIsNotFound(Pair<Optional<Payment>, String> initialPaymentAndApprovalCode) {
        String approvalCode = initialPaymentAndApprovalCode.getRight();
        if (Objects.nonNull(approvalCode)) {
            Optional<Payment> optionalInitialPayment = initialPaymentAndApprovalCode.getLeft();
            if (optionalInitialPayment.isEmpty()) {
                logger.log(Level.ERROR, "Invalid Refund Attempt, No Payment is associated with given approvalCode: {}", approvalCode);
                throw new InvalidTransactionAttemptException("Invalid Refund Attempt, No Payment is associated with given approvalCode: " + approvalCode);
            }
        }
    }

    private void throwExceptionIfTransactionAuthChainIdIsAlreadyUsed(Pair<Optional<List<Payment>>, String> optionalInitialAuthPayment, List<Payment> payments) {
        String transactionAuthChainId = optionalInitialAuthPayment.getRight();
        List<TransactionType> transactionTypes = payments.stream().map(Payment::getTransactionType).distinct().collect(Collectors.toList());
        logger.log(Level.ERROR, "Invalid Refund Attempt, Given transactionAuthChainId: {} is already used for Transaction Type/s: {}", transactionAuthChainId, transactionTypes);
        throw new InvalidTransactionAttemptException("Invalid Refund Attempt, Given transactionAuthChainId: " + transactionAuthChainId + " is already used for Transaction Type/s: " + transactionTypes);
    }

    public void logWarningForInvalidRequestData(CPPaymentRefundRequest request) {
        DateHelper.logWarningForInvalidTransactionDate(request.getTransactionDateTime());
    }

    public void throwExceptionIfDuplicateTransactionIdUsed(Optional<List<Payment>> paymentsByTransactionId) {
        if(paymentsByTransactionId.isPresent()){
            List<Payment> paymentsList = paymentsByTransactionId.get();
            if(!paymentsList.isEmpty()) {
                Optional<Payment> refundTransactions = paymentsList.stream().filter(p -> TransactionType.REFUND.equals(p.getTransactionType())).findFirst();
                if (refundTransactions.isPresent()) {
                    Payment payment = refundTransactions.get();
                    String mgmTransactionId = payment.getMgmTransactionId();
                    String authChainId = payment.getAuthChainId();
                    logger.log(Level.ERROR, "Invalid Refund Attempt, Given transactionId in Headers: {} is already used for transactionAuthChainId: {}", mgmTransactionId, authChainId);
                    throw new InvalidTransactionAttemptException("Invalid Refund Attempt, Given transactionId in Headers: " + mgmTransactionId + " is already used for transactionAuthChainId: " + authChainId);
                }
            }
        }
    }
}
