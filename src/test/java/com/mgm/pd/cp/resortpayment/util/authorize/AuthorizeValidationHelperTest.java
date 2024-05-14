package com.mgm.pd.cp.resortpayment.util.authorize;

import com.mgm.pd.cp.payment.common.constant.AuthType;
import com.mgm.pd.cp.payment.common.constant.TransactionType;
import com.mgm.pd.cp.payment.common.exception.InvalidTransactionAttemptException;
import com.mgm.pd.cp.payment.common.model.Payment;
import com.mgm.pd.cp.resortpayment.dto.authorize.CPPaymentAuthorizationRequest;
import com.mgm.pd.cp.resortpayment.exception.InvalidTransactionTypeException;
import com.mgm.pd.cp.resortpayment.util.TestHelperUtil;
import org.flywaydb.core.internal.util.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@SpringBootTest
class AuthorizeValidationHelperTest {
    @Test
    void should_throwExceptionIfDuplicateTransactionIdUsed() throws IOException {
        //given
        Optional<List<Payment>> initialPayment = TestHelperUtil.getInitialPayment();
        initialPayment.get().stream().findFirst().get().setTransactionType(TransactionType.AUTHORIZE);

        //then
        Assertions.assertThrows(InvalidTransactionAttemptException.class, () -> AuthorizeValidationHelper.throwExceptionIfDuplicateTransactionIdUsed(initialPayment));
    }

    @Test
    void should_throwExceptionForInvalidAttempts() throws IOException {
        //given
        Pair<Optional<List<Payment>>, String> optionalInitialAuthPayment = Pair.of(TestHelperUtil.getInitialPayment(), "12345");

        //then
        Assertions.assertThrows(InvalidTransactionAttemptException.class, () -> AuthorizeValidationHelper.throwExceptionForInvalidAttempts(optionalInitialAuthPayment));
    }

    @Test
    void should_throwExceptionForInvalidRequest_CardPresentField() throws IOException {
        CPPaymentAuthorizationRequest request = TestHelperUtil.getAuthorizationRequest();
        request.getTransactionDetails().setIsCardPresent(true);

        //then
        Assertions.assertThrows(InvalidTransactionAttemptException.class, () -> AuthorizeValidationHelper.throwExceptionForInvalidRequest(request));
    }

    @Test
    void should_throwExceptionForInvalidRequest_TransactionType() throws IOException {
        CPPaymentAuthorizationRequest request = TestHelperUtil.getAuthorizationRequest();
        request.setTransactionType(AuthType.SUPP);

        //then
        Assertions.assertThrows(InvalidTransactionTypeException.class, () -> AuthorizeValidationHelper.throwExceptionForInvalidRequest(request));
    }

    @Test
    void should_throwExceptionForInvalidRequest_CardExpired() throws IOException {
        CPPaymentAuthorizationRequest request = TestHelperUtil.getAuthorizationRequest();
        request.getTransactionDetails().getCard().setExpiryDate("0122");

        //then
        Assertions.assertThrows(InvalidTransactionAttemptException.class, () -> AuthorizeValidationHelper.throwExceptionForInvalidRequest(request));
    }
}
