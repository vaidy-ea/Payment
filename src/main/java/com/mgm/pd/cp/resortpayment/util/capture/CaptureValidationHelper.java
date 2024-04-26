package com.mgm.pd.cp.resortpayment.util.capture;

import com.mgm.pd.cp.payment.common.constant.AuthType;
import com.mgm.pd.cp.payment.common.constant.TransactionType;
import com.mgm.pd.cp.payment.common.model.Payment;
import com.mgm.pd.cp.resortpayment.dto.capture.CPPaymentCaptureRequest;
import com.mgm.pd.cp.resortpayment.exception.InvalidTransactionAttemptException;
import com.mgm.pd.cp.resortpayment.exception.InvalidTransactionTypeException;
import com.mgm.pd.cp.resortpayment.exception.MissingRequiredFieldException;
import lombok.experimental.UtilityClass;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.flywaydb.core.internal.util.Pair;
import org.springframework.http.HttpHeaders;

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
                throwExceptionIfVoidOrRefundAlreadyDone(optionalInitialAuthPayment, payments);
                throwExceptionIfDifferentMGMTokenUsed(request, optionalInitialAuthPayment, payments);
            }
        }
    }

    public void throwExceptionIfRequiredFieldMissing(CPPaymentCaptureRequest request) {
        if(request.getTransactionAuthChainId() == null || request.getTransactionAuthChainId().isEmpty()) {
            throw new MissingRequiredFieldException("transactionAuthChainId can't be empty or NULL");
        }
    }

    private static void throwExceptionIfVoidOrRefundAlreadyDone(Pair<Optional<List<Payment>>, String> optionalInitialAuthPayment, List<Payment> payments) {
        long count = payments.stream().filter(p -> (Objects.isNull(p.getAuthSubType()) && TransactionType.VOID.equals(p.getTransactionType()))
                || TransactionType.REFUND.equals(p.getTransactionType())).count();
        if (count > 0) {
            String transactionAuthChainId = optionalInitialAuthPayment.getRight();
            List<TransactionType> transactionTypes = payments.stream().map(Payment::getTransactionType).distinct().collect(Collectors.toList());
            logger.log(Level.ERROR, "Invalid Capture Attempt, Given transactionAuthChainId: {} is already used for Transaction Type/s: {}", transactionAuthChainId, transactionTypes);
            throw new InvalidTransactionAttemptException("Invalid Capture Attempt, Given transactionAuthChainId: " + transactionAuthChainId + " is already used for Transaction Type/s: " + transactionTypes);
        }
    }

    private static void throwExceptionIfDifferentMGMTokenUsed(CPPaymentCaptureRequest request, Pair<Optional<List<Payment>>, String> optionalInitialAuthPayment, List<Payment> payments) {
        String tokenValue = request.getTransactionDetails().getCard().getTokenValue();
        String parentMGMToken = payments.stream().filter(p -> Objects.isNull(p.getReferenceId()) && TransactionType.AUTHORIZE.equals(p.getTransactionType())).map(Payment::getMgmToken).collect(Collectors.joining());
        if (!parentMGMToken.equals(tokenValue)) {
            String transactionAuthChainId = optionalInitialAuthPayment.getRight();
            logger.log(Level.ERROR, "Invalid Capture Attempt, MGM token used for parent Initial Auth Transaction is different what is used now for transactionAuthChainId: {}", transactionAuthChainId);
            throw new InvalidTransactionAttemptException("Invalid Capture Attempt, MGM token used for parent Initial Auth Transaction is different what is used now for transactionAuthChainId: " + transactionAuthChainId);
        }
    }

    public static void logWarningIfDifferentClientIdUsed(HttpHeaders headers, Pair<Optional<List<Payment>>, String> optionalInitialAuthPayment) {
        Optional<List<Payment>> optionalPaymentList = optionalInitialAuthPayment.getLeft();
        if(optionalPaymentList.isPresent()) {
            List<Payment> payments = optionalPaymentList.get();
            if (!payments.isEmpty()) {
                String parentClientId = payments.stream().filter(p -> Objects.isNull(p.getReferenceId()) && TransactionType.AUTHORIZE.equals(p.getTransactionType())).map(Payment::getClientId).collect(Collectors.joining());
                String requestClientId = headers.toSingleValueMap().get(MGM_CLIENT_ID);
                if (!parentClientId.equals(requestClientId)) {
                    logger.log(Level.WARN, "Client Id used is different for Initial Auth and Capture for the given transactionAuthChainId: {}", optionalInitialAuthPayment.getRight());
                }
            }
        }
    }
}
