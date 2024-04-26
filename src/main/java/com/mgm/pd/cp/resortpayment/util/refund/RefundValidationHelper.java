package com.mgm.pd.cp.resortpayment.util.refund;

import com.mgm.pd.cp.payment.common.constant.TransactionType;
import com.mgm.pd.cp.payment.common.model.Payment;
import com.mgm.pd.cp.resortpayment.exception.InvalidTransactionAttemptException;
import lombok.experimental.UtilityClass;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.flywaydb.core.internal.util.Pair;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@UtilityClass
public class RefundValidationHelper {
    private static final Logger logger = LogManager.getLogger(RefundValidationHelper.class);

    public void throwExceptionForInvalidAttempts(Pair<Optional<List<Payment>>, String> optionalInitialAuthPayment) {
        Optional<List<Payment>> optionalPaymentList = optionalInitialAuthPayment.getLeft();
        if(optionalPaymentList.isPresent()) {
            List<Payment> payments = optionalPaymentList.get();
            if (!payments.isEmpty()) {
                throwExceptionIfTransactionAuthChainIdIsAlreadyUsed(optionalInitialAuthPayment, payments);
            }
        }
    }

    private static void throwExceptionIfTransactionAuthChainIdIsAlreadyUsed(Pair<Optional<List<Payment>>, String> optionalInitialAuthPayment, List<Payment> payments) {
        String transactionAuthChainId = optionalInitialAuthPayment.getRight();
        List<TransactionType> transactionTypes = payments.stream().map(Payment::getTransactionType).distinct().collect(Collectors.toList());
        logger.log(Level.ERROR, "Invalid Refund Attempt, Given transactionAuthChainId: {} is already used for Transaction Type/s: {}", transactionAuthChainId, transactionTypes);
        throw new InvalidTransactionAttemptException("Invalid Refund Attempt, Given transactionAuthChainId: " + transactionAuthChainId + " is already used for Transaction Type/s: " + transactionTypes);
    }
}
