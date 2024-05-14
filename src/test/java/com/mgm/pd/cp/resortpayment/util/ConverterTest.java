package com.mgm.pd.cp.resortpayment.util;

import com.mgm.pd.cp.resortpayment.dto.authorize.CPPaymentAuthorizationRequest;
import com.mgm.pd.cp.resortpayment.dto.capture.CPPaymentCaptureRequest;
import com.mgm.pd.cp.resortpayment.dto.cardvoid.CPPaymentCardVoidRequest;
import com.mgm.pd.cp.resortpayment.dto.common.BaseTransactionDetails;
import com.mgm.pd.cp.resortpayment.dto.common.TransactionDetails;
import com.mgm.pd.cp.resortpayment.dto.incrementalauth.CPPaymentIncrementalAuthRequest;
import com.mgm.pd.cp.resortpayment.dto.refund.CPPaymentRefundRequest;
import com.mgm.pd.cp.resortpayment.dto.router.RouterRequest;
import com.mgm.pd.cp.resortpayment.util.authorize.AuthorizeToRouterConverter;
import com.mgm.pd.cp.resortpayment.util.capture.CaptureToRouterConverter;
import com.mgm.pd.cp.resortpayment.util.cardvoid.VoidToRouterConverter;
import com.mgm.pd.cp.resortpayment.util.common.PaymentProcessingServiceHelper;
import com.mgm.pd.cp.resortpayment.util.incremental.IncrementalToRouterConverter;
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
    @Autowired
    AuthorizeToRouterConverter authorizeToRouterConverter;
    @Autowired
    CaptureToRouterConverter captureToRouterConverter;
    @Autowired
    IncrementalToRouterConverter incrementalToRouterConverter;

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
        CPPaymentCardVoidRequest request = TestHelperUtil.getVoidPaymentRequest();
        request.setHeaders(TestHelperUtil.buildCustomHeaders());
        request.getTransactionDetails().setCard(null);
        request.setOriginalTransactionIdentifier(null);
        Mockito.when(paymentProcessingServiceHelper.getSaleDetailsObject(BaseTransactionDetails.builder().build())).thenReturn(null);
        RouterRequest convert = voidToRouterConverter.convert(request);

        //then
        assert convert != null;
        Assertions.assertFalse(convert.getRequestJson().isEmpty());
    }

    @Test
    void test_authorize_converter() throws IOException {
        //given
        CPPaymentAuthorizationRequest request = TestHelperUtil.getAuthorizationRequest();
        request.setHeaders(TestHelperUtil.buildCustomHeaders());
        TransactionDetails transactionDetails = request.getTransactionDetails();
        transactionDetails.setCustomer(null);
        transactionDetails.setIsCardPresent(null);
        transactionDetails.getTransactionAmount().setDetailedAmount(null);
        request.setOriginalTransactionIdentifier(null);
        Mockito.when(paymentProcessingServiceHelper.getBaseTransactionDetails(request)).thenReturn(new BaseTransactionDetails());
        Mockito.when(paymentProcessingServiceHelper.getSaleDetailsObject(BaseTransactionDetails.builder().build())).thenReturn(null);
        RouterRequest convert = authorizeToRouterConverter.convert(request);

        //then
        assert convert != null;
        Assertions.assertFalse(convert.getRequestJson().isEmpty());
    }

    @Test
    void test_capture_converter() throws IOException {
        //given
        CPPaymentCaptureRequest request = TestHelperUtil.getCapturePaymentRequest();
        request.setHeaders(TestHelperUtil.buildCustomHeaders());
        TransactionDetails transactionDetails = request.getTransactionDetails();
        transactionDetails.setCustomer(null);
        transactionDetails.setIsCardPresent(null);
        transactionDetails.getTransactionAmount().setDetailedAmount(null);
        request.setOriginalTransactionIdentifier(null);
        Mockito.when(paymentProcessingServiceHelper.getBaseTransactionDetails(request)).thenReturn(new BaseTransactionDetails());
        Mockito.when(paymentProcessingServiceHelper.getSaleDetailsObject(BaseTransactionDetails.builder().build())).thenReturn(null);
        RouterRequest convert = captureToRouterConverter.convert(request);

        //then
        assert convert != null;
        Assertions.assertFalse(convert.getRequestJson().isEmpty());
    }

    @Test
    void test_incremental_converter() throws IOException {
        //given
        CPPaymentIncrementalAuthRequest request = TestHelperUtil.getIncrementalAuthRequest();
        request.setHeaders(TestHelperUtil.buildCustomHeaders());
        TransactionDetails transactionDetails = request.getTransactionDetails();
        transactionDetails.setCustomer(null);
        transactionDetails.setIsCardPresent(null);
        transactionDetails.getTransactionAmount().setDetailedAmount(null);
        request.setOriginalTransactionIdentifier(null);
        Mockito.when(paymentProcessingServiceHelper.getBaseTransactionDetails(request)).thenReturn(new BaseTransactionDetails());
        Mockito.when(paymentProcessingServiceHelper.getSaleDetailsObject(BaseTransactionDetails.builder().build())).thenReturn(null);
        RouterRequest convert = incrementalToRouterConverter.convert(request);

        //then
        assert convert != null;
        Assertions.assertFalse(convert.getRequestJson().isEmpty());
    }
}
