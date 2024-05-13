package com.mgm.pd.cp.resortpayment.util.cardvoid;

import com.mgm.pd.cp.payment.common.constant.AuthType;
import com.mgm.pd.cp.payment.common.constant.TransactionType;
import com.mgm.pd.cp.payment.common.exception.InvalidTransactionAttemptException;
import com.mgm.pd.cp.payment.common.model.Payment;
import com.mgm.pd.cp.resortpayment.dto.cardvoid.CPPaymentCardVoidRequest;
import com.mgm.pd.cp.resortpayment.exception.InvalidTransactionTypeException;
import com.mgm.pd.cp.resortpayment.exception.MissingRequiredFieldException;
import com.mgm.pd.cp.resortpayment.util.common.DateHelper;
import lombok.experimental.UtilityClass;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.flywaydb.core.internal.util.Pair;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@UtilityClass
public class CardVoidValidationHelper {
    private static final Logger logger = LogManager.getLogger(CardVoidValidationHelper.class);
    private static final List<AuthType> APPROVED_VOID_TRANSACTION_TYPES = List.of(AuthType.CREDIT);

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
                throwExceptionIfVoidOrRefundAlreadyDone(optionalInitialAuthPayment, payments);
            }
        }
    }

    private void throwExceptionIfVoidOrRefundAlreadyDone(Pair<Optional<List<Payment>>, String> optionalInitialAuthPayment, List<Payment> payments) {
        long count = payments.stream().filter(p -> TransactionType.REFUND.equals(p.getTransactionType()) || TransactionType.VOID.equals(p.getTransactionType())).count();
        if (count > 0) {
            String transactionAuthChainId = optionalInitialAuthPayment.getRight();
            List<TransactionType> transactionTypes = payments.stream().map(Payment::getTransactionType).distinct().collect(Collectors.toList());
            logger.log(Level.ERROR, "Invalid Card Void Attempt, Given transactionAuthChainId: {} is already used for Transaction Type/s: {}", transactionAuthChainId, transactionTypes);
            throw new InvalidTransactionAttemptException("Invalid Card Void Attempt, Given transactionAuthChainId: " + transactionAuthChainId + " is already used for Transaction Type/s: " + transactionTypes);
        }
    }

    public void logWarningForInvalidRequestData(CPPaymentCardVoidRequest request) {
        DateHelper.logWarningForInvalidTransactionDate(request.getTransactionDateTime());
    }

    public void logWarningForInvalidRequest(Pair<Optional<List<Payment>>, String> optionalInitialAuthPayment, CPPaymentCardVoidRequest request) {
        Optional<List<Payment>> optionalPaymentList = optionalInitialAuthPayment.getLeft();
        if(optionalPaymentList.isPresent()) {
            List<Payment> payments = optionalPaymentList.get();
            if (!payments.isEmpty()) {
                logWarningIfDifferentBusinessDateUsed(request.getTransactionDateTime(), optionalInitialAuthPayment, payments);
            }
        }
    }

    private void logWarningIfDifferentBusinessDateUsed(String transactionDateTime, Pair<Optional<List<Payment>>, String> optionalInitialAuthPayment, List<Payment> payments) {
        LocalDate currentTransactionDate = ZonedDateTime.parse(transactionDateTime).toLocalDate();
        LocalDateTime parentTransactionDateTime = payments.get(payments.size() - 1).getCreatedTimeStamp();
        LocalDate parentTransactionDate = parentTransactionDateTime.toLocalDate();
        if (currentTransactionDate.isBefore(parentTransactionDate) || currentTransactionDate.isAfter(parentTransactionDate)) {
            logger.log(Level.WARN, "transactionDateTime used for Parent Transaction: {} is different from transactionDateTime used for Void: {} for the given transactionAuthChainId: {}", parentTransactionDateTime, transactionDateTime, optionalInitialAuthPayment.getRight());
        }
    }

    public void throwExceptionIfDuplicateTransactionIdUsed(Optional<List<Payment>> paymentsByTransactionId) {
        if(paymentsByTransactionId.isPresent()){
            List<Payment> paymentsList = paymentsByTransactionId.get();
            if(!paymentsList.isEmpty()) {
                Optional<Payment> voidTransaction = paymentsList.stream().filter(p -> TransactionType.VOID.equals(p.getTransactionType())).findFirst();
                if (voidTransaction.isPresent()) {
                    Payment payment = voidTransaction.get();
                    String mgmTransactionId = payment.getMgmTransactionId();
                    String authChainId = payment.getAuthChainId();
                    logger.log(Level.ERROR, "Invalid Card Void Attempt, Given transactionId in Headers: {} is already used for transactionAuthChainId: {}", mgmTransactionId, authChainId);
                    throw new InvalidTransactionAttemptException("Invalid Card Void Attempt, Given transactionId in Headers: " + mgmTransactionId + " is already used for transactionAuthChainId: " + authChainId);
                }
            }
        }
    }

    public void throwExceptionForInvalidRequest(CPPaymentCardVoidRequest request) {
        throwExceptionIfRequiredFieldMissing(request);
        throwExceptionIfTransactionTypeIsInvalid(request);
    }

    private void throwExceptionIfTransactionTypeIsInvalid(CPPaymentCardVoidRequest request) {
        AuthType transactionType = request.getTransactionType();
        if(Objects.nonNull(transactionType) && !APPROVED_VOID_TRANSACTION_TYPES.contains(transactionType)) {
            logger.log(Level.WARN, "Invalid Transaction Type received in Card Void request is: {}", transactionType);
            logger.log(Level.ERROR, "Invalid Transaction Type received in Card Void request is: {}", transactionType);
            throw new InvalidTransactionTypeException("Invalid field transactionType, Possible values is/are: "+ APPROVED_VOID_TRANSACTION_TYPES);
        }
    }
}
