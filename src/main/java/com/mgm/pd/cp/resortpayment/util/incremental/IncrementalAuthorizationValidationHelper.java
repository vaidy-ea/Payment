package com.mgm.pd.cp.resortpayment.util.incremental;

import com.mgm.pd.cp.payment.common.constant.AuthType;
import com.mgm.pd.cp.payment.common.constant.TransactionType;
import com.mgm.pd.cp.payment.common.model.Payment;
import com.mgm.pd.cp.resortpayment.dto.incrementalauth.CPPaymentIncrementalAuthRequest;
import com.mgm.pd.cp.resortpayment.exception.InvalidTransactionAttemptException;
import com.mgm.pd.cp.resortpayment.exception.InvalidTransactionTypeException;
import com.mgm.pd.cp.resortpayment.exception.MissingRequiredFieldException;
import com.mgm.pd.cp.resortpayment.util.common.DateHelper;
import lombok.experimental.UtilityClass;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.flywaydb.core.internal.util.Pair;
import org.springframework.http.HttpHeaders;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.mgm.pd.cp.payment.common.constant.ApplicationConstants.MGM_CLIENT_ID;

@UtilityClass
public class IncrementalAuthorizationValidationHelper {
    private static final Logger logger = LogManager.getLogger(IncrementalAuthorizationValidationHelper.class);
    private static final List<AuthType> APPROVED_INCREMENTAL_AUTHORIZATION_TRANSACTION_TYPES = List.of(AuthType.SUPP);
    public void throwExceptionIfRequiredFieldMissing(CPPaymentIncrementalAuthRequest request) {
        if(request.getTransactionAuthChainId() == null || request.getTransactionAuthChainId().isEmpty()) {
            throw new MissingRequiredFieldException("transactionAuthChainId can't be empty or NULL");
        }
    }

    public void throwExceptionIfTransactionTypeIsInvalid(CPPaymentIncrementalAuthRequest request) {
        AuthType transactionType = request.getTransactionType();
        if(Objects.nonNull(transactionType) && !APPROVED_INCREMENTAL_AUTHORIZATION_TRANSACTION_TYPES.contains(transactionType)) {
            logger.log(Level.WARN, "Invalid Transaction Type received in Incremental Auth request is: {}", transactionType);
            logger.log(Level.ERROR, "Invalid Transaction Type received in Incremental Auth request is: {}", transactionType);
            throw new InvalidTransactionTypeException("Invalid field transactionType, Possible values is/are: "+ APPROVED_INCREMENTAL_AUTHORIZATION_TRANSACTION_TYPES);
        }
    }

    public void throwExceptionForInvalidAttempts(CPPaymentIncrementalAuthRequest request, Pair<Optional<List<Payment>>, String> optionalInitialAuthPayment) {
        Optional<List<Payment>> optionalPaymentList = optionalInitialAuthPayment.getLeft();
        if(optionalPaymentList.isPresent()) {
            List<Payment> payments = optionalPaymentList.get();
            if (!payments.isEmpty()) {
                throwExceptionIfCaptureOrRefundOrVoidAlreadyDone(optionalInitialAuthPayment, payments);
                throwExceptionIfDifferentMGMTokenUsed(request, optionalInitialAuthPayment, payments);
            }
        }
    }

    private void throwExceptionIfDifferentMGMTokenUsed(CPPaymentIncrementalAuthRequest request, Pair<Optional<List<Payment>>, String> optionalInitialAuthPayment, List<Payment> payments) {
        String tokenValue = request.getTransactionDetails().getCard().getTokenValue();
        String parentMGMToken = payments.stream().filter(p -> Objects.isNull(p.getReferenceId()) && TransactionType.AUTHORIZE.equals(p.getTransactionType())).map(Payment::getMgmToken).collect(Collectors.joining());
        if (!parentMGMToken.equals(tokenValue)) {
            String transactionAuthChainId = optionalInitialAuthPayment.getRight();
            logger.log(Level.ERROR, "Invalid Incremental Authorization Attempt, MGM token used for parent Initial Auth Transaction is different what is used now for transactionAuthChainId: {}", transactionAuthChainId);
            throw new InvalidTransactionAttemptException("Invalid Incremental Authorization Attempt, MGM token used for parent Initial Auth Transaction is different what is used now for transactionAuthChainId: " + transactionAuthChainId);
        }
    }

    private void throwExceptionIfCaptureOrRefundOrVoidAlreadyDone(Pair<Optional<List<Payment>>, String> optionalInitialAuthPayment, List<Payment> payments) {
        long count = payments.stream().filter(p -> (Objects.isNull(p.getAuthSubType()) && TransactionType.VOID.equals(p.getTransactionType()))
                || TransactionType.CAPTURE.equals(p.getTransactionType())
                || TransactionType.REFUND.equals(p.getTransactionType())).count();
        if (count > 0) {
            String transactionAuthChainId = optionalInitialAuthPayment.getRight();
            List<TransactionType> transactionTypes = payments.stream().map(Payment::getTransactionType).distinct().collect(Collectors.toList());
            logger.log(Level.ERROR, "Invalid Incremental Authorization Attempt, Given transactionAuthChainId: {} is already used for Transaction Type/s: {}", transactionAuthChainId, transactionTypes);
            throw new InvalidTransactionAttemptException("Invalid Incremental Authorization Attempt, Given transactionAuthChainId: " + transactionAuthChainId + " is already used for Transaction Type/s: " + transactionTypes);
        }
    }

    public void logWarningForInvalidRequest(HttpHeaders headers, Pair<Optional<List<Payment>>, String> optionalInitialAuthPayment, CPPaymentIncrementalAuthRequest request) {
        Optional<List<Payment>> optionalPaymentList = optionalInitialAuthPayment.getLeft();
        if(optionalPaymentList.isPresent()) {
            List<Payment> payments = optionalPaymentList.get();
            if (!payments.isEmpty()) {
                logWarningIfDifferentBusinessDateUsed(request.getTransactionDateTime(), optionalInitialAuthPayment, payments);
                logWarningIfDifferentClientIdUsed(headers, optionalInitialAuthPayment, payments);
            }
        }
    }

    private void logWarningIfDifferentBusinessDateUsed(String transactionDateTime, Pair<Optional<List<Payment>>, String> optionalInitialAuthPayment, List<Payment> payments) {
        LocalDate currentTransactionDate = ZonedDateTime.parse(transactionDateTime).toLocalDate();
        LocalDateTime parentTransactionDateTime = payments.get(payments.size() - 1).getCreatedTimeStamp();
        LocalDate parentTransactionDate = parentTransactionDateTime.toLocalDate();
        if (currentTransactionDate.isBefore(parentTransactionDate) || currentTransactionDate.isAfter(parentTransactionDate)) {
            logger.log(Level.WARN, "transactionDateTime used for Parent Transaction: {} is different from transactionDateTime used for Incremental Auth: {} for the given transactionAuthChainId: {}", parentTransactionDateTime, transactionDateTime, optionalInitialAuthPayment.getRight());
        }
    }

    private void logWarningIfDifferentClientIdUsed(HttpHeaders headers, Pair<Optional<List<Payment>>, String> optionalInitialAuthPayment, List<Payment> payments) {
        String parentClientId = payments.stream().filter(p -> Objects.isNull(p.getReferenceId()) && TransactionType.AUTHORIZE.equals(p.getTransactionType())).map(Payment::getClientId).collect(Collectors.joining());
        String requestClientId = headers.toSingleValueMap().get(MGM_CLIENT_ID);
        if (!parentClientId.equals(requestClientId)) {
            logger.log(Level.WARN, "Client Id used is different for Initial Auth and Incremental Auth for the given transactionAuthChainId: {}", optionalInitialAuthPayment.getRight());
        }
    }

    public void logWarningForInvalidRequestData(CPPaymentIncrementalAuthRequest request) {
        DateHelper.logWarningForInvalidTransactionDate(request.getTransactionDateTime());
    }
}
