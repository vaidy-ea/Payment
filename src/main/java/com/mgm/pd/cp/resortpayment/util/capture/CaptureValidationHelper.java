package com.mgm.pd.cp.resortpayment.util.capture;

import com.mgm.pd.cp.payment.common.constant.AuthType;
import com.mgm.pd.cp.payment.common.constant.TransactionType;
import com.mgm.pd.cp.payment.common.exception.InvalidTransactionAttemptException;
import com.mgm.pd.cp.payment.common.model.Payment;
import com.mgm.pd.cp.resortpayment.dto.capture.CPPaymentCaptureRequest;
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
public class CaptureValidationHelper {
    private static final Logger logger = LogManager.getLogger(CaptureValidationHelper.class);
    private static final List<AuthType> APPROVED_CAPTURE_TRANSACTION_TYPES = List.of(AuthType.CHECKOUT, AuthType.DEPOSIT);

    public void throwExceptionIfTransactionTypeIsInvalid(CPPaymentCaptureRequest request) {
        AuthType transactionType = request.getTransactionType();
        if(Objects.nonNull(transactionType) && !APPROVED_CAPTURE_TRANSACTION_TYPES.contains(transactionType)) {
            logger.log(Level.WARN, "Invalid Transaction Type received in Capture request is: {}", transactionType);
            logger.log(Level.ERROR, "Invalid Transaction Type received in Capture request is: {}", transactionType);
            throw new InvalidTransactionTypeException("Invalid field transactionType, Possible values is/are: "+ APPROVED_CAPTURE_TRANSACTION_TYPES);
        }
    }

    public void throwExceptionForInvalidAttempts(CPPaymentCaptureRequest request, Pair<Optional<List<Payment>>, String> optionalInitialAuthPayment) {
        Optional<List<Payment>> optionalPaymentList = optionalInitialAuthPayment.getLeft();
        if(optionalPaymentList.isPresent()) {
            List<Payment> payments = optionalPaymentList.get();
            if (!payments.isEmpty()) {
                throwExceptionIfCaptureOrVoidOrRefundAlreadyDone(optionalInitialAuthPayment, payments);
                throwExceptionIfDifferentMGMTokenUsed(request, optionalInitialAuthPayment, payments);
            }
        }
    }

    private void throwExceptionIfCardIsExpired(CPPaymentCaptureRequest request) throws ParseException {
        String cardExpiryDate = request.getTransactionDetails().getCard().getExpiryDate();
        if (Objects.nonNull(cardExpiryDate)) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMyy");
            simpleDateFormat.setLenient(false);
            if (simpleDateFormat.parse(cardExpiryDate).before(simpleDateFormat.parse(new SimpleDateFormat("MMyy").format(new Date())))) {
                logger.log(Level.ERROR, "Invalid Capture Attempt, Card has already expired with expiry date: {}", cardExpiryDate);
                throw new InvalidTransactionAttemptException("Invalid Capture Attempt, Card has already expired with expiry date: " + cardExpiryDate);
            }
        }
    }

    public void throwExceptionIfRequiredFieldMissing(CPPaymentCaptureRequest request) {
        if(request.getTransactionAuthChainId() == null || request.getTransactionAuthChainId().isEmpty()) {
            throw new MissingRequiredFieldException("transactionAuthChainId can't be empty or NULL");
        }
    }

    private void throwExceptionIfCaptureOrVoidOrRefundAlreadyDone(Pair<Optional<List<Payment>>, String> optionalInitialAuthPayment, List<Payment> payments) {
        long count = payments.stream().filter(p -> (Objects.isNull(p.getAuthSubType()) && TransactionType.VOID.equals(p.getTransactionType()))
                || TransactionType.REFUND.equals(p.getTransactionType())
                || TransactionType.CAPTURE.equals(p.getTransactionType())).count();
        if (count > 0) {
            String transactionAuthChainId = optionalInitialAuthPayment.getRight();
            List<TransactionType> transactionTypes = payments.stream().map(Payment::getTransactionType).distinct().collect(Collectors.toList());
            logger.log(Level.ERROR, "Invalid Capture Attempt, Given transactionAuthChainId: {} is already used for Transaction Type/s: {}", transactionAuthChainId, transactionTypes);
            throw new InvalidTransactionAttemptException("Invalid Capture Attempt, Given transactionAuthChainId: " + transactionAuthChainId + " is already used for Transaction Type/s: " + transactionTypes);
        }
    }

    private void throwExceptionIfDifferentMGMTokenUsed(CPPaymentCaptureRequest request, Pair<Optional<List<Payment>>, String> optionalInitialAuthPayment, List<Payment> payments) {
        String tokenValue = request.getTransactionDetails().getCard().getTokenValue();
        String parentMGMToken = payments.stream().filter(p -> Objects.isNull(p.getReferenceId()) && TransactionType.AUTHORIZE.equals(p.getTransactionType())).map(Payment::getMgmToken).collect(Collectors.joining());
        if (!parentMGMToken.equals(tokenValue)) {
            String transactionAuthChainId = optionalInitialAuthPayment.getRight();
            logger.log(Level.ERROR, "Invalid Capture Attempt, MGM token used for parent Initial Auth Transaction is different what is used now for transactionAuthChainId: {}", transactionAuthChainId);
            throw new InvalidTransactionAttemptException("Invalid Capture Attempt, MGM token used for parent Initial Auth Transaction is different what is used now for transactionAuthChainId: " + transactionAuthChainId);
        }
    }

    public void logWarningForInvalidRequest(HttpHeaders headers, Pair<Optional<List<Payment>>, String> optionalInitialAuthPayment, CPPaymentCaptureRequest request) {
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
            logger.log(Level.WARN, "transactionDateTime used for Parent Transaction: {} is different from transactionDateTime used for Capture: {} for the given transactionAuthChainId: {}", parentTransactionDateTime, transactionDateTime, optionalInitialAuthPayment.getRight());
        }
    }

    private void logWarningIfDifferentClientIdUsed(HttpHeaders headers, Pair<Optional<List<Payment>>, String> optionalInitialAuthPayment, List<Payment> payments) {
        String parentClientId = payments.stream().filter(p -> Objects.isNull(p.getReferenceId()) && TransactionType.AUTHORIZE.equals(p.getTransactionType())).map(Payment::getClientId).collect(Collectors.joining());
        String requestClientId = headers.toSingleValueMap().get(MGM_CLIENT_ID);
        if (!parentClientId.equals(requestClientId)) {
            logger.log(Level.WARN, "Client Id used is different for Initial Auth and Capture for the given transactionAuthChainId: {}", optionalInitialAuthPayment.getRight());
        }
    }

    public void logWarningForInvalidRequestData(CPPaymentCaptureRequest request) {
        DateHelper.logWarningForInvalidTransactionDate(request.getTransactionDateTime());
    }

    public void throwExceptionIfDuplicateTransactionIdUsed(Optional<List<Payment>> paymentsByTransactionId) {
        if(paymentsByTransactionId.isPresent()){
            List<Payment> paymentsList = paymentsByTransactionId.get();
            if(!paymentsList.isEmpty()) {
                Optional<Payment> captureTransactions = paymentsList.stream().filter(p -> TransactionType.CAPTURE.equals(p.getTransactionType())).findFirst();
                if (captureTransactions.isPresent()) {
                    Payment payment = captureTransactions.get();
                    String mgmTransactionId = payment.getMgmTransactionId();
                    String authChainId = payment.getAuthChainId();
                    logger.log(Level.ERROR, "Invalid Capture Attempt, Given transactionId in Headers: {} is already used for transactionAuthChainId: {}", mgmTransactionId, authChainId);
                    throw new InvalidTransactionAttemptException("Invalid Capture Attempt, Given transactionId in Headers: " + mgmTransactionId + " is already used for transactionAuthChainId: " + authChainId);
                }
            }
        }
    }

    public void throwExceptionIfCardPresentIsTrue(CPPaymentCaptureRequest request) {
        Boolean isCardPresent = request.getTransactionDetails().getIsCardPresent();
        if (Objects.nonNull(isCardPresent) && isCardPresent) {
            logger.log(Level.ERROR, "Invalid Capture Auth Attempt, isCardPresent field should be false");
            throw new InvalidTransactionAttemptException("Invalid Capture Auth Attempt, isCardPresent field should be false");
        }
    }

    public void throwExceptionForInvalidRequest(CPPaymentCaptureRequest request) throws ParseException {
        throwExceptionIfRequiredFieldMissing(request);
        throwExceptionIfTransactionTypeIsInvalid(request);
        throwExceptionIfCardIsExpired(request);
        throwExceptionIfCardPresentIsTrue(request);
    }
}
