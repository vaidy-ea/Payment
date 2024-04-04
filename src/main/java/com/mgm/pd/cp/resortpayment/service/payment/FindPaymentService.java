package com.mgm.pd.cp.resortpayment.service.payment;

import com.mgm.pd.cp.payment.common.constant.AuthType;
import com.mgm.pd.cp.payment.common.model.Payment;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

/**
 * This interface defines the contract to find the details
 * from Payment DB
 */
public interface FindPaymentService {

    /**
     *
     * @param authChainId: first parameter to math in db records
     * @param transactionType: second parameter to math in db records
     * @return Optional<List<Payment>>: List of Payments on the basis of authChainId and transactionType if present
     */
    @Transactional
    Optional<List<Payment>> getPaymentDetails(String authChainId, @Valid AuthType transactionType);

    /**
     *
     * @param authChainId: parameter to math in db records
     * @return Optional<List<Payment>>: List of Payments on the basis of authChainId if present
     */
    @Transactional
    Optional<List<Payment>> getPaymentDetails(String authChainId);
}
