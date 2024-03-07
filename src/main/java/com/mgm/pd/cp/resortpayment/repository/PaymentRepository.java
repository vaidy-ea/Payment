package com.mgm.pd.cp.resortpayment.repository;

import com.mgm.pd.cp.payment.common.constant.AuthType;
import com.mgm.pd.cp.payment.common.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, String> {
    //Optional<Payment> findFirstByPropertyCodeAndResvNameIDAndAuthTypeNotNullAndApprovalCodeNotNullOrderByIdDesc(String resortId, String reservationNumber);
    Optional<List<Payment>> findByAuthChainIdAndAuthSubType(Long authChainId, @Valid AuthType transactionType);

    Optional<List<Payment>> findByAuthChainId(Long authChainId);
}
