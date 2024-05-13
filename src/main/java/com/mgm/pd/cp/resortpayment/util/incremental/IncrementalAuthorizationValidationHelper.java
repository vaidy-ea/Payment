package com.mgm.pd.cp.resortpayment.util.incremental;

import com.mgm.pd.cp.payment.common.constant.AuthType;
import com.mgm.pd.cp.payment.common.constant.TransactionType;
import com.mgm.pd.cp.payment.common.exception.InvalidTransactionAttemptException;
import com.mgm.pd.cp.payment.common.model.Payment;
import com.mgm.pd.cp.resortpayment.dto.incrementalauth.CPPaymentIncrementalAuthRequest;
import com.mgm.pd.cp.resortpayment.exception.InvalidTransactionTypeException;
import com.mgm.pd.cp.resortpayment.exception.MissingRequiredFieldException;
import com.mgm.pd.cp.resortpayment.util.common.DateHelper;
import lombok.experimental.UtilityClass;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.flywaydb.core.internal.util.Pair;
import org.springframework.http.HttpHeaders;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Date;
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
        String transactionAuthChainId = request.getTransactionAuthChainId();
        if(transactionAuthChainId == null || transactionAuthChainId.isEmpty()) {
            throw new MissingRequiredFieldException("transactionAuthChainId can't be empty or NULL");
        }
        Double cumulativeAmount = request.getTransactionDetails().getTransactionAmount().getCumulativeAmount();
        if(cumulativeAmount == null || cumulativeAmount.isNaN()) {
            throw new MissingRequiredFieldException("cumulativeAmount can't be empty or NULL");
        }
    }

    public void throwExceptionIfTransactionTypeIsInvalid(CPPaymentIncrementalAuthRequest request) {
        AuthType transactionType = request.getTransactionType();
        if(Objects.nonNull(transactionType) && !APPROVED_INCREMENTAL_AUTHORIZATION_TRANSACTION_TYPES.contains(transactionType)) {
            logger.log(Level.WARN, "Invalid Transaction Type received in Incremental Auth request is: {}", transactionType);
            logger.log(Level.ERROR, "Invalid Transaction Type received in Incremental Auth request is: {}", transactionType);
            throw new InvalidTransactionTypeException("Invalid field transactionType, Possible values is/are: " + APPROVED_INCREMENTAL_AUTHORIZATION_TRANSACTION_TYPES);
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

    private static void throwExceptionIfCumulativeAmountIsLessThanRequestedAmount(CPPaymentIncrementalAuthRequest request) {
        Double requestedAmount = request.getTransactionDetails().getTransactionAmount().getRequestedAmount();
        Double cumulativeAmount = request.getTransactionDetails().getTransactionAmount().getCumulativeAmount();
        if (cumulativeAmount < requestedAmount) {
            throw new InvalidTransactionAttemptException("Cumulative Amount should be greater than or equals to requestedAmount");
        }
    }

    private void throwExceptionIfCardIsExpired(CPPaymentIncrementalAuthRequest request) throws ParseException {
        String cardExpiryDate = request.getTransactionDetails().getCard().getExpiryDate();
        if (Objects.nonNull(cardExpiryDate)) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMyy");
            simpleDateFormat.setLenient(false);
            if (simpleDateFormat.parse(cardExpiryDate).before(simpleDateFormat.parse(new SimpleDateFormat("MMyy").format(new Date())))) {
                logger.log(Level.ERROR, "Invalid Incremental Auth Attempt, Card has already expired with expiry date: {}", cardExpiryDate);
                throw new InvalidTransactionAttemptException("Invalid Incremental Auth Attempt, Card has already expired with expiry date: " + cardExpiryDate);
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
        DateHelper.logWarningForInvalidTransactionDate(request.getTransactionDateTime());
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

    public void throwExceptionIfDuplicateTransactionIdUsed(Optional<List<Payment>> paymentsByTransactionId) {
        if(paymentsByTransactionId.isPresent()){
            List<Payment> paymentsList = paymentsByTransactionId.get();
            if(!paymentsList.isEmpty()) {
                Optional<Payment> incrementalAuthTransactions = paymentsList.stream().filter(p -> Objects.nonNull(p.getReferenceId()) && AuthType.SUPP.equals(p.getAuthSubType())
                        && TransactionType.AUTHORIZE.equals(p.getTransactionType())).findFirst();
                if (incrementalAuthTransactions.isPresent()) {
                    Payment payment = incrementalAuthTransactions.get();
                    String mgmTransactionId = payment.getMgmTransactionId();
                    String authChainId = payment.getAuthChainId();
                    logger.log(Level.ERROR, "Invalid Incremental Authorization Attempt, Given transactionId in Headers: {} is already used for transactionAuthChainId: {}", mgmTransactionId, authChainId);
                    throw new InvalidTransactionAttemptException("Invalid Incremental Authorization Attempt, Given transactionId in Headers: " + mgmTransactionId + " is already used for transactionAuthChainId: " + authChainId);
                }
            }
        }
    }

    public void throwExceptionIfCardPresentIsTrue(CPPaymentIncrementalAuthRequest request) {
        Boolean isCardPresent = request.getTransactionDetails().getIsCardPresent();
        if (Objects.nonNull(isCardPresent) && isCardPresent) {
            logger.log(Level.ERROR, "Invalid Incremental Auth Attempt, isCardPresent field should be false");
            throw new InvalidTransactionAttemptException("Invalid Incremental Auth Attempt, isCardPresent field should be false");
        }
    }

    public void throwExceptionForInvalidRequest(CPPaymentIncrementalAuthRequest request) throws ParseException {
        throwExceptionIfRequiredFieldMissing(request);
        throwExceptionIfTransactionTypeIsInvalid(request);
        throwExceptionIfCardIsExpired(request);
        throwExceptionIfCumulativeAmountIsLessThanRequestedAmount(request);
        throwExceptionIfCardPresentIsTrue(request);
    }
}
