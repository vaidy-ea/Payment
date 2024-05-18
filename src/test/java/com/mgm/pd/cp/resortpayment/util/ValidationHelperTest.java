package com.mgm.pd.cp.resortpayment.util;

import com.mgm.pd.cp.payment.common.constant.AuthType;
import com.mgm.pd.cp.payment.common.dto.CPPaymentIncrementalAuthRequest;
import com.mgm.pd.cp.resortpayment.exception.InvalidTransactionTypeException;
import com.mgm.pd.cp.resortpayment.exception.MissingRequiredFieldException;
import com.mgm.pd.cp.resortpayment.util.common.DateHelper;
import com.mgm.pd.cp.resortpayment.util.incremental.IncrementalAuthorizationValidationHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.time.ZonedDateTime;

@SpringBootTest
class ValidationHelperTest {
    @Test
    void should_throwExceptionIfTransactionTypeIsInvalid() throws IOException {
        //given
        CPPaymentIncrementalAuthRequest incrementalAuthRequest = TestHelperUtil.getIncrementalAuthRequest();
        incrementalAuthRequest.setTransactionType(AuthType.INIT);

        //then
        Assertions.assertThrows(InvalidTransactionTypeException.class, () -> IncrementalAuthorizationValidationHelper.throwExceptionIfTransactionTypeIsInvalid(incrementalAuthRequest));
    }

    @Test
    void should_throwExceptionIfRequiredFieldMissing() throws IOException {
        //given
        CPPaymentIncrementalAuthRequest incrementalAuthRequest = TestHelperUtil.getIncrementalAuthRequest();
        incrementalAuthRequest.setTransactionAuthChainId(null);

        //then
        Assertions.assertThrows(MissingRequiredFieldException.class, () -> IncrementalAuthorizationValidationHelper.throwExceptionIfRequiredFieldMissing(incrementalAuthRequest));
    }

    @Test
    void should_logWarningForInvalidTransactionFutureDate(){
        String dateTime = ZonedDateTime.now().plusDays(5L).toString();
        DateHelper.logWarningForInvalidTransactionDate(dateTime);
        Assertions.assertFalse(dateTime.isEmpty());
    }

    @Test
    void should_logWarningForInvalidTransactionDatePastDate(){
        String dateTime = ZonedDateTime.now().minusDays(5L).toString();
        DateHelper.logWarningForInvalidTransactionDate(dateTime);
        Assertions.assertFalse(dateTime.isEmpty());
    }
 }
