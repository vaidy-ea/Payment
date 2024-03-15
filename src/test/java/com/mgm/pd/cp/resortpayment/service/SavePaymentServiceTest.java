package com.mgm.pd.cp.resortpayment.service;

import com.mgm.pd.cp.resortpayment.repository.PaymentRepository;
import com.mgm.pd.cp.resortpayment.service.payment.SavePaymentServiceImpl;
import com.mgm.pd.cp.resortpayment.util.TestHelperUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

@SpringBootTest
class SavePaymentServiceTest {

    @Autowired
    SavePaymentServiceImpl paymentService;

    @Autowired
    PaymentRepository paymentRepository;

    @Test
    void test_incremental_payment_with_payload() throws IOException {
        paymentRepository.deleteAll();
        paymentService.saveIncrementalAuthorizationPayment(TestHelperUtil.getIncrementalAuthRequestWithHeaders(), TestHelperUtil.getIncrementalRouterResponse(), TestHelperUtil.getInitialPayment().get().get(0));
        Assertions.assertEquals(1, paymentRepository.findAll().size());
    }

    @Test
    void test_refund_payment_with_payload() throws IOException {
        paymentRepository.deleteAll();
        paymentService.saveRefundPayment(TestHelperUtil.getRefundPaymentRequestWithHeaders(), TestHelperUtil.getRefundRouterResponse(), TestHelperUtil.getInitialPayment().get().get(0));
        Assertions.assertEquals(1, paymentRepository.findAll().size());
    }

}
