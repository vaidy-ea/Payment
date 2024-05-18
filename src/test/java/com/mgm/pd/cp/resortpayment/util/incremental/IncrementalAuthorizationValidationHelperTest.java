package com.mgm.pd.cp.resortpayment.util.incremental;

import com.mgm.pd.cp.payment.common.constant.AuthType;
import com.mgm.pd.cp.payment.common.constant.TransactionType;
import com.mgm.pd.cp.payment.common.dto.CPPaymentIncrementalAuthRequest;
import com.mgm.pd.cp.payment.common.exception.InvalidTransactionAttemptException;
import com.mgm.pd.cp.payment.common.model.Payment;
import com.mgm.pd.cp.resortpayment.exception.MissingRequiredFieldException;
import com.mgm.pd.cp.resortpayment.util.TestHelperUtil;
import org.flywaydb.core.internal.util.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@SpringBootTest
public class IncrementalAuthorizationValidationHelperTest {
    @Test
    void should_throwExceptionIfDuplicateTransactionIdUsed() throws IOException {
        //given
        Optional<List<Payment>> initialPayment = TestHelperUtil.getInitialPayment();
        Payment payment = initialPayment.get().stream().findFirst().get();
        payment.setTransactionType(TransactionType.AUTHORIZE);
        payment.setAuthSubType(AuthType.SUPP);
        payment.setReferenceId("123");

        //then
        Assertions.assertThrows(InvalidTransactionAttemptException.class, () -> IncrementalAuthorizationValidationHelper.throwExceptionIfDuplicateTransactionIdUsed(initialPayment));
    }

    @Test
    void should_throwExceptionVoidAlreadyDone() throws IOException {
        //given
        CPPaymentIncrementalAuthRequest request = TestHelperUtil.getIncrementalAuthRequest();
        Optional<List<Payment>> initialPayment = TestHelperUtil.getInitialPayment();
        initialPayment.get().stream().findFirst().get().setAuthSubType(null);
        initialPayment.get().stream().findFirst().get().setTransactionType(TransactionType.VOID);
        Pair<Optional<List<Payment>>, String> pair = Pair.of(initialPayment, "12345");

        //then
        Assertions.assertThrows(InvalidTransactionAttemptException.class, () -> IncrementalAuthorizationValidationHelper.throwExceptionForInvalidAttempts(request, pair));
    }

    @Test
    void should_throwExceptionCaptureAlreadyDone() throws IOException {
        //given
        CPPaymentIncrementalAuthRequest request = TestHelperUtil.getIncrementalAuthRequest();
        Optional<List<Payment>> initialPayment = TestHelperUtil.getInitialPayment();
        initialPayment.get().stream().findFirst().get().setTransactionType(TransactionType.CAPTURE);
        Pair<Optional<List<Payment>>, String> pair = Pair.of(initialPayment, "12345");

        //then
        Assertions.assertThrows(InvalidTransactionAttemptException.class, () -> IncrementalAuthorizationValidationHelper.throwExceptionForInvalidAttempts(request, pair));
    }

    @Test
    void should_throwExceptionRefundAlreadyDone() throws IOException {
        //given
        CPPaymentIncrementalAuthRequest request = TestHelperUtil.getIncrementalAuthRequest();
        Optional<List<Payment>> initialPayment = TestHelperUtil.getInitialPayment();
        initialPayment.get().stream().findFirst().get().setTransactionType(TransactionType.REFUND);
        Pair<Optional<List<Payment>>, String> pair = Pair.of(initialPayment, "12345");

        //then
        Assertions.assertThrows(InvalidTransactionAttemptException.class, () -> IncrementalAuthorizationValidationHelper.throwExceptionForInvalidAttempts(request, pair));
    }

    @Test
    void should_throwExceptionForInvalidAttempts_DifferentMGMToken() throws IOException {
        //given
        CPPaymentIncrementalAuthRequest request = TestHelperUtil.getIncrementalAuthRequest();
        Optional<List<Payment>> initialPayment = TestHelperUtil.getInitialPayment();
        initialPayment.get().stream().findFirst().get().setTransactionType(TransactionType.AUTHORIZE);
        initialPayment.get().stream().findFirst().get().setMgmToken("1234567890");
        Pair<Optional<List<Payment>>, String> pair = Pair.of(initialPayment, "12345");

        //then
        Assertions.assertThrows(InvalidTransactionAttemptException.class, () -> IncrementalAuthorizationValidationHelper.throwExceptionForInvalidAttempts(request, pair));
    }

    @Test
    void should_throwExceptionForInvalidRequest_CardExpired() throws IOException {
        CPPaymentIncrementalAuthRequest request = TestHelperUtil.getIncrementalAuthRequest();
        request.getTransactionDetails().getCard().setExpiryDate("0122");

        //then
        Assertions.assertThrows(InvalidTransactionAttemptException.class, () -> IncrementalAuthorizationValidationHelper.throwExceptionForInvalidRequest(request));
    }

    @Test
    void should_throwExceptionForInvalidRequest_CardPresentFieldShouldBeFalse() throws IOException {
        CPPaymentIncrementalAuthRequest request = TestHelperUtil.getIncrementalAuthRequest();
        request.getTransactionDetails().setIsCardPresent(true);

        //then
        Assertions.assertThrows(InvalidTransactionAttemptException.class, () -> IncrementalAuthorizationValidationHelper.throwExceptionForInvalidRequest(request));
    }

    @Test
    void should_throwExceptionForInvalidRequest_TransactionAuthChainId() throws IOException {
        CPPaymentIncrementalAuthRequest request = TestHelperUtil.getIncrementalAuthRequest();
        request.setTransactionAuthChainId(null);

        //then
        Assertions.assertThrows(MissingRequiredFieldException.class, () -> IncrementalAuthorizationValidationHelper.throwExceptionForInvalidRequest(request));
    }

    @Test
    void should_throwExceptionForInvalidRequest_CumulativeAmount() throws IOException {
        CPPaymentIncrementalAuthRequest request = TestHelperUtil.getIncrementalAuthRequest();
        request.getTransactionDetails().getTransactionAmount().setCumulativeAmount(null);

        //then
        Assertions.assertThrows(MissingRequiredFieldException.class, () -> IncrementalAuthorizationValidationHelper.throwExceptionForInvalidRequest(request));
    }

    @Test
    void should_throwExceptionForInvalidRequest_CumulativeAmountLessThanRequestedAmount() throws IOException {
        CPPaymentIncrementalAuthRequest request = TestHelperUtil.getIncrementalAuthRequest();
        request.getTransactionDetails().getTransactionAmount().setCumulativeAmount(Double.valueOf("1"));

        //then
        Assertions.assertThrows(InvalidTransactionAttemptException.class, () -> IncrementalAuthorizationValidationHelper.throwExceptionForInvalidRequest(request));
    }
}
