package com.mgm.pd.cp.resortpayment.util.cardvoid;


import com.mgm.pd.cp.payment.common.constant.AuthType;
import com.mgm.pd.cp.payment.common.constant.TransactionType;
import com.mgm.pd.cp.payment.common.dto.CPPaymentCardVoidRequest;
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
class CardVoidValidationHelperTest {

    @Test
    void should_throwExceptionIfRequiredFieldMissing() throws IOException {
        CPPaymentCardVoidRequest voidPaymentRequest = TestHelperUtil.getVoidPaymentRequest();
        voidPaymentRequest.setTransactionAuthChainId(null);

        //then
        Assertions.assertThrows(MissingRequiredFieldException.class, () -> CardVoidValidationHelper.throwExceptionIfRequiredFieldMissing(voidPaymentRequest));
    }

    @Test
    void should_throwExceptionForInvalidAttempts() throws IOException {
        //given
        Optional<List<Payment>> initialPayment = TestHelperUtil.getInitialPayment();
        initialPayment.get().stream().findFirst().get().setTransactionType(TransactionType.REFUND);
        Pair<Optional<List<Payment>>, String> optionalInitialAuthPayment = Pair.of(initialPayment, "OK1234");

        //then
        Assertions.assertThrows(InvalidTransactionAttemptException.class, () -> CardVoidValidationHelper.throwExceptionForInvalidAttempts(optionalInitialAuthPayment));
    }

    @Test
    void should_throwExceptionIfDuplicateTransactionIdUsed() throws IOException {
        //given
        Optional<List<Payment>> initialPayment = TestHelperUtil.getInitialPayment();
        initialPayment.get().stream().findFirst().get().setTransactionType(TransactionType.VOID);

        //then
        Assertions.assertThrows(InvalidTransactionAttemptException.class, () -> CardVoidValidationHelper.throwExceptionIfDuplicateTransactionIdUsed(initialPayment));
    }

    @Test
    void should_throwExceptionForInvalidRequest() throws IOException {
        CPPaymentCardVoidRequest voidPaymentRequest = TestHelperUtil.getVoidPaymentRequest();
        voidPaymentRequest.setTransactionType(AuthType.INIT);

        //then
        Assertions.assertThrows(InvalidTransactionTypeException.class, () -> CardVoidValidationHelper.throwExceptionForInvalidRequest(voidPaymentRequest));
    }
}
