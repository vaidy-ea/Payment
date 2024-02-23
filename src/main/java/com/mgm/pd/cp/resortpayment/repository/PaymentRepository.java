package com.mgm.pd.cp.resortpayment.repository;

import com.mgm.pd.cp.resortpayment.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findFirstByPropertyCodeAndResvNameIDAndAuthTypeNotNullAndApprovalCodeNotNullOrderByIdDesc(String resortId, String reservationNumber);
}