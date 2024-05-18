package com.mgm.pd.cp.resortpayment.util.capture;

import com.mgm.pd.cp.payment.common.constant.AuthType;
import com.mgm.pd.cp.payment.common.constant.TransactionType;
import com.mgm.pd.cp.payment.common.dto.CPPaymentCaptureRequest;
import com.mgm.pd.cp.payment.common.exception.InvalidTransactionAttemptException;
import com.mgm.pd.cp.payment.common.model.Payment;
import com.mgm.pd.cp.resortpayment.exception.InvalidTransactionTypeException;
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
class CaptureValidationHelperTest {
    @Test
    void should_throwExceptionForInvalidAttempts() throws IOException {
        //given
        CPPaymentCaptureRequest request = TestHelperUtil.getCapturePaymentRequest();
        Optional<List<Payment>> initialPayment = TestHelperUtil.getInitialPayment();
        initialPayment.get().stream().findFirst().get().setTransactionType(TransactionType.CAPTURE);
        Pair<Optional<List<Payment>>, String> pair = Pair.of(initialPayment, "12345");

        //then
        Assertions.assertThrows(InvalidTransactionAttemptException.class, () -> CaptureValidationHelper.throwExceptionForInvalidAttempts(request, pair));
    }

    @Test
    void should_throwExceptionForInvalidAttempts_DifferentMGMToken() throws IOException {
        //given
        CPPaymentCaptureRequest request = TestHelperUtil.getCapturePaymentRequest();
        Optional<List<Payment>> initialPayment = TestHelperUtil.getInitialPayment();
        initialPayment.get().stream().findFirst().get().setTransactionType(TransactionType.AUTHORIZE);
        initialPayment.get().stream().findFirst().get().setMgmToken("1234567890");
        Pair<Optional<List<Payment>>, String> pair = Pair.of(initialPayment, "12345");

        //then
        Assertions.assertThrows(InvalidTransactionAttemptException.class, () -> CaptureValidationHelper.throwExceptionForInvalidAttempts(request, pair));
    }

    @Test
    void should_throwExceptionCaptureOrVoidOrRefundAlreadyDone() throws IOException {
        //given
        CPPaymentCaptureRequest request = TestHelperUtil.getCapturePaymentRequest();
        Optional<List<Payment>> initialPayment = TestHelperUtil.getInitialPayment();
        initialPayment.get().stream().findFirst().get().setAuthSubType(null);
        initialPayment.get().stream().findFirst().get().setTransactionType(TransactionType.VOID);
        Pair<Optional<List<Payment>>, String> pair = Pair.of(initialPayment, "12345");

        //then
        Assertions.assertThrows(InvalidTransactionAttemptException.class, () -> CaptureValidationHelper.throwExceptionForInvalidAttempts(request, pair));
    }

    @Test
    void should_throwExceptionIfCaptureOrVoidOrRefundAlreadyDone() throws IOException {
        //given
        CPPaymentCaptureRequest request = TestHelperUtil.getCapturePaymentRequest();
        Optional<List<Payment>> initialPayment = TestHelperUtil.getInitialPayment();
        initialPayment.get().stream().findFirst().get().setTransactionType(TransactionType.REFUND);
        Pair<Optional<List<Payment>>, String> pair = Pair.of(initialPayment, "12345");

        //then
        Assertions.assertThrows(InvalidTransactionAttemptException.class, () -> CaptureValidationHelper.throwExceptionForInvalidAttempts(request, pair));
    }

    @Test
    void should_throwExceptionIfDuplicateTransactionIdUsed() throws IOException {
        //given
        Optional<List<Payment>> initialPayment = TestHelperUtil.getInitialPayment();
        initialPayment.get().stream().findFirst().get().setTransactionType(TransactionType.CAPTURE);

        //then
        Assertions.assertThrows(InvalidTransactionAttemptException.class, () -> CaptureValidationHelper.throwExceptionIfDuplicateTransactionIdUsed(initialPayment));
    }

    @Test
    void should_throwExceptionForInvalidRequest_CardPresentFieldShouldBeFalse() throws IOException {
        CPPaymentCaptureRequest request = TestHelperUtil.getCapturePaymentRequest();
        request.getTransactionDetails().setIsCardPresent(true);

        //then
        Assertions.assertThrows(InvalidTransactionAttemptException.class, () -> CaptureValidationHelper.throwExceptionForInvalidRequest(request));
    }

    @Test
    void should_throwExceptionForInvalidRequest_TransactionAuthChainId() throws IOException {
        CPPaymentCaptureRequest request = TestHelperUtil.getCapturePaymentRequest();
        request.setTransactionAuthChainId(null);

        //then
        Assertions.assertThrows(MissingRequiredFieldException.class, () -> CaptureValidationHelper.throwExceptionForInvalidRequest(request));
    }

    @Test
    void should_throwExceptionForInvalidRequest_TransactionTypeIsInvalid() throws IOException {
        CPPaymentCaptureRequest request = TestHelperUtil.getCapturePaymentRequest();
        request.setTransactionType(AuthType.SUPP);

        //then
        Assertions.assertThrows(InvalidTransactionTypeException.class, () -> CaptureValidationHelper.throwExceptionIfTransactionTypeIsInvalid(request));
    }

    @Test
    void should_throwExceptionForInvalidRequest_CardExpired() throws IOException {
        CPPaymentCaptureRequest request = TestHelperUtil.getCapturePaymentRequest();
        request.getTransactionDetails().getCard().setExpiryDate("0122");

        //then
        Assertions.assertThrows(InvalidTransactionAttemptException.class, () -> CaptureValidationHelper.throwExceptionForInvalidRequest(request));
    }
}
