package com.mgm.pd.cp.resortpayment.service.payment;

import com.mgm.pd.cp.payment.common.model.Payment;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface FindPaymentService {
    @Transactional
    Optional<List<Payment>> getPaymentDetails(Long authChainId);
}
