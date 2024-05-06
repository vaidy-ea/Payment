package com.mgm.pd.cp.resortpayment.util.authorize;

import com.mgm.pd.cp.payment.common.constant.AuthType;
import com.mgm.pd.cp.payment.common.constant.TransactionType;
import com.mgm.pd.cp.payment.common.exception.InvalidTransactionAttemptException;
import com.mgm.pd.cp.payment.common.model.Payment;
import com.mgm.pd.cp.resortpayment.dto.authorize.CPPaymentAuthorizationRequest;
import com.mgm.pd.cp.resortpayment.exception.InvalidTransactionTypeException;
import com.mgm.pd.cp.resortpayment.util.common.DateHelper;
import lombok.experimental.UtilityClass;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.flywaydb.core.internal.util.Pair;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@UtilityClass
public class AuthorizeValidationHelper {
    private static final Logger logger = LogManager.getLogger(AuthorizeValidationHelper.class);
    private static final List<AuthType> APPROVED_AUTHORIZATION_TRANSACTION_TYPES = List.of(AuthType.INIT, AuthType.DEPOSIT, AuthType.AR);

    public void throwExceptionIfTransactionTypeIsInvalid(CPPaymentAuthorizationRequest request) {
        AuthType transactionType = request.getTransactionType();
        if(Objects.nonNull(transactionType) && !APPROVED_AUTHORIZATION_TRANSACTION_TYPES.contains(transactionType)) {
            logger.log(Level.WARN, "Invalid Transaction Type received in Initial Auth request is: {}", transactionType);
            logger.log(Level.ERROR, "Invalid Transaction Type received in Initial Auth request is: {}", transactionType);
            throw new InvalidTransactionTypeException("Invalid field transactionType, Possible values is/are: "+ APPROVED_AUTHORIZATION_TRANSACTION_TYPES);
        }
    }

    public void throwExceptionIfCardIsExpired(CPPaymentAuthorizationRequest request) throws ParseException {
        String cardExpiryDate = request.getTransactionDetails().getCard().getExpiryDate();
        if (Objects.nonNull(cardExpiryDate)) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMyy");
            simpleDateFormat.setLenient(false);
            if (simpleDateFormat.parse(cardExpiryDate).before(simpleDateFormat.parse(new SimpleDateFormat("MMyy").format(new Date())))) {
                logger.log(Level.ERROR, "Invalid Initial Auth Attempt, Card has already expired with expiry date: {}", cardExpiryDate);
                throw new InvalidTransactionAttemptException("Invalid Initial Auth Attempt, Card has already expired with expiry date: " + cardExpiryDate);
            }
        }
    }

    public void throwExceptionForInvalidAttempts(CPPaymentAuthorizationRequest request, Pair<Optional<List<Payment>>, String> optionalInitialAuthPayment) throws ParseException {
        throwExceptionIfTransactionTypeIsInvalid(request);
        throwExceptionIfCardIsExpired(request);
        Optional<List<Payment>> optionalPaymentList = optionalInitialAuthPayment.getLeft();
        if(optionalPaymentList.isPresent()) {
            List<Payment> payments = optionalPaymentList.get();
            if (!payments.isEmpty()) {
                throwExceptionIfTransactionAuthChainIdIsAlreadyUsed(optionalInitialAuthPayment, payments);
            }
        }
    }

    private void throwExceptionIfTransactionAuthChainIdIsAlreadyUsed(Pair<Optional<List<Payment>>, String> optionalInitialAuthPayment, List<Payment> payments) {
        String transactionAuthChainId = optionalInitialAuthPayment.getRight();
        List<TransactionType> transactionTypes = payments.stream().map(Payment::getTransactionType).distinct().collect(Collectors.toList());
        logger.log(Level.ERROR, "Invalid Initial Auth Attempt, Given transactionAuthChainId: {} is already used for Transaction Type/s: {}", transactionAuthChainId, transactionTypes);
        throw new InvalidTransactionAttemptException("Invalid Initial Auth Attempt, Given transactionAuthChainId: " + transactionAuthChainId + " is already used for Transaction Type/s: " + transactionTypes);
    }

    public void logWarningForInvalidRequestData(CPPaymentAuthorizationRequest request) {
        DateHelper.logWarningForInvalidTransactionDate(request.getTransactionDateTime());
    }
}
