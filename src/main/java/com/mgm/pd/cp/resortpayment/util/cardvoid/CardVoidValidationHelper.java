package com.mgm.pd.cp.resortpayment.util.cardvoid;

import com.mgm.pd.cp.payment.common.constant.TransactionType;
import com.mgm.pd.cp.payment.common.model.Payment;
import com.mgm.pd.cp.resortpayment.dto.cardvoid.CPPaymentCardVoidRequest;
import com.mgm.pd.cp.resortpayment.exception.InvalidTransactionAttemptException;
import com.mgm.pd.cp.resortpayment.exception.MissingRequiredFieldException;
import lombok.experimental.UtilityClass;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.flywaydb.core.internal.util.Pair;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@UtilityClass
public class CardVoidValidationHelper {
    private static final Logger logger = LogManager.getLogger(CardVoidValidationHelper.class);

    public void throwExceptionIfRequiredFieldMissing(CPPaymentCardVoidRequest request) {
        if(request.getTransactionAuthChainId() == null || request.getTransactionAuthChainId().isEmpty()) {
            throw new MissingRequiredFieldException("transactionAuthChainId can't be empty or NULL");
        }
    }

    public void throwExceptionForInvalidAttempts(Pair<Optional<List<Payment>>, String> optionalInitialAuthPayment) {
        Optional<List<Payment>> optionalPaymentList = optionalInitialAuthPayment.getLeft();
        if(optionalPaymentList.isPresent()) {
            List<Payment> payments = optionalPaymentList.get();
            if (!payments.isEmpty()) {
                throwExceptionIfRefundAlreadyDone(optionalInitialAuthPayment, payments);
            }
        }
    }

    private static void throwExceptionIfRefundAlreadyDone(Pair<Optional<List<Payment>>, String> optionalInitialAuthPayment, List<Payment> payments) {
        long count = payments.stream().filter(p -> TransactionType.REFUND.equals(p.getTransactionType())).count();
        if (count > 0) {
            String transactionAuthChainId = optionalInitialAuthPayment.getRight();
            List<TransactionType> transactionTypes = payments.stream().map(Payment::getTransactionType).distinct().collect(Collectors.toList());
            logger.log(Level.ERROR, "Invalid Card Void Attempt, Given transactionAuthChainId: {} is already used for Transaction Type/s: {}", transactionAuthChainId, transactionTypes);
            throw new InvalidTransactionAttemptException("Invalid Card Void Attempt, Given transactionAuthChainId: " + transactionAuthChainId + " is already used for Transaction Type/s: " + transactionTypes);
        }
    }
}
