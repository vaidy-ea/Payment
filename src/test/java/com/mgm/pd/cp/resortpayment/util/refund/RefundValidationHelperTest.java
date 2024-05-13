package com.mgm.pd.cp.resortpayment.util.refund;

import com.mgm.pd.cp.payment.common.constant.TransactionType;
import com.mgm.pd.cp.payment.common.exception.InvalidTransactionAttemptException;
import com.mgm.pd.cp.payment.common.model.Payment;
import com.mgm.pd.cp.resortpayment.dto.refund.CPPaymentRefundRequest;
import com.mgm.pd.cp.resortpayment.util.TestHelperUtil;
import org.flywaydb.core.internal.util.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@SpringBootTest
class RefundValidationHelperTest {

    @Test
    void should_throwExceptionIfDuplicateTransactionIdUsed() throws IOException {
        //given
        Optional<List<Payment>> initialPayment = TestHelperUtil.getInitialPayment();
        initialPayment.get().stream().findFirst().get().setTransactionType(TransactionType.REFUND);

        //then
        Assertions.assertThrows(InvalidTransactionAttemptException.class, () -> RefundValidationHelper.throwExceptionIfDuplicateTransactionIdUsed(initialPayment));
    }

    @Test
    void should_throwExceptionIfApprovalCodeIsNotFound() throws IOException {
        //given
        Pair<Optional<List<Payment>>, String> optionalInitialAuthPayment = Pair.of(TestHelperUtil.getInitialPayment(), "OK1234");
        Pair<Optional<Payment>, String> initialPaymentAndApprovalCode = Pair.of(Optional.empty(), "OK1234");

        //then
        Assertions.assertThrows(InvalidTransactionAttemptException.class, () -> RefundValidationHelper.throwExceptionForInvalidAttempts(optionalInitialAuthPayment, initialPaymentAndApprovalCode));
    }

    @Test
    void should_throwExceptionIfCardPresentIsTrue() throws IOException {
        CPPaymentRefundRequest refundPaymentRequest = TestHelperUtil.getRefundPaymentRequest();
        refundPaymentRequest.getTransactionDetails().setIsCardPresent(Boolean.TRUE);

        //then
        Assertions.assertThrows(InvalidTransactionAttemptException.class, () -> RefundValidationHelper.throwExceptionIfCardPresentIsTrue(refundPaymentRequest));
    }

    @Test
    void should_throwExceptionIfTransactionAuthChainIdIsAlreadyUsed() throws IOException {
        //given
        Pair<Optional<List<Payment>>, String> optionalInitialAuthPayment = Pair.of(TestHelperUtil.getInitialPayment(), "OK1234");
        Pair<Optional<Payment>, String> initialPaymentAndApprovalCode = Pair.of(TestHelperUtil.getInitialPayment().get().stream().findAny(), "OK1234");

        //then
        Assertions.assertThrows(InvalidTransactionAttemptException.class, () -> RefundValidationHelper.throwExceptionForInvalidAttempts(optionalInitialAuthPayment, initialPaymentAndApprovalCode));
    }
}
