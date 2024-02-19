package com.mgm.pd.cp.resortpayment.service;

import com.mgm.pd.cp.resortpayment.model.Payment;

import java.util.Optional;

public interface FindPaymentService {
    Optional<Payment> getPaymentDetails(String resortId, String reservationNumber);
}
