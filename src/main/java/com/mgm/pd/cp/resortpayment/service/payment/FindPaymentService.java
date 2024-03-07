package com.mgm.pd.cp.resortpayment.service.payment;

import com.mgm.pd.cp.payment.common.constant.AuthType;
import com.mgm.pd.cp.payment.common.model.Payment;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

public interface FindPaymentService {
    @Transactional
    Optional<List<Payment>> getPaymentDetails(Long authChainId, @Valid AuthType transactionType);

    @Transactional
    Optional<List<Payment>> getPaymentDetails(Long authChainId);
}
