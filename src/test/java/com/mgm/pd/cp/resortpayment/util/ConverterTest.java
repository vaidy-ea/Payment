package com.mgm.pd.cp.resortpayment.util;

import com.mgm.pd.cp.resortpayment.dto.cardvoid.CPPaymentCardVoidRequest;
import com.mgm.pd.cp.resortpayment.dto.common.BaseTransactionDetails;
import com.mgm.pd.cp.resortpayment.dto.common.TransactionDetails;
import com.mgm.pd.cp.resortpayment.dto.refund.CPPaymentRefundRequest;
import com.mgm.pd.cp.resortpayment.dto.router.RouterRequest;
import com.mgm.pd.cp.resortpayment.util.cardvoid.VoidToRouterConverter;
import com.mgm.pd.cp.resortpayment.util.common.PaymentProcessingServiceHelper;
import com.mgm.pd.cp.resortpayment.util.refund.RefundToRouterConverter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.io.IOException;

@SpringBootTest
class ConverterTest {
    @Autowired
    RefundToRouterConverter refundToRouterConverter;
    @Autowired
    VoidToRouterConverter voidToRouterConverter;
    @MockBean
    PaymentProcessingServiceHelper paymentProcessingServiceHelper;

    @Test
    void test_refund_converter() throws IOException {
        //given
        CPPaymentRefundRequest refundPaymentRequest = TestHelperUtil.getRefundPaymentRequestWithHeaders();
        TransactionDetails transactionDetails = refundPaymentRequest.getTransactionDetails();
        transactionDetails.setCustomer(null);
        transactionDetails.setIsCardPresent(null);
        transactionDetails.getTransactionAmount().setDetailedAmount(null);
        refundPaymentRequest.setOriginalTransactionIdentifier(null);
        Mockito.when(paymentProcessingServiceHelper.getBaseTransactionDetails(refundPaymentRequest)).thenReturn(new BaseTransactionDetails());
        Mockito.when(paymentProcessingServiceHelper.getSaleDetailsObject(BaseTransactionDetails.builder().build())).thenReturn(null);
        //when
        RouterRequest convert = refundToRouterConverter.convert(refundPaymentRequest);

        //then
        assert convert != null;
        Assertions.assertFalse(convert.getRequestJson().isEmpty());
    }

    @Test
    void test_void_converter() throws IOException {
        //given
        CPPaymentCardVoidRequest voidPaymentRequest = TestHelperUtil.getVoidPaymentRequest();
        voidPaymentRequest.setHeaders(TestHelperUtil.buildCustomHeaders());
        voidPaymentRequest.getTransactionDetails().setCard(null);
        voidPaymentRequest.setOriginalTransactionIdentifier(null);
        Mockito.when(paymentProcessingServiceHelper.getSaleDetailsObject(BaseTransactionDetails.builder().build())).thenReturn(null);
        RouterRequest convert = voidToRouterConverter.convert(voidPaymentRequest);

        //then
        assert convert != null;
        Assertions.assertFalse(convert.getRequestJson().isEmpty());
    }
}
