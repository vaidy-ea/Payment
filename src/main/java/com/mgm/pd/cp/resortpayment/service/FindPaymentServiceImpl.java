package com.mgm.pd.cp.resortpayment.service;

import com.mgm.pd.cp.resortpayment.model.Payment;
import com.mgm.pd.cp.resortpayment.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class FindPaymentServiceImpl implements FindPaymentService {

    @Autowired
    PaymentRepository paymentRepository;

    @Override
    public Optional<Payment> getPaymentDetails(String resortId, String reservationNumber) {
        return paymentRepository.findFirstByPropertyCodeAndResvNameIDOrderByIdDesc(resortId, reservationNumber);
        //return paymentRepository.findFirstByPropertyCodeAndResvNameIDAndAuthTypeNotNullOrderByIdDesc(resortId, reservationNumber);
    }
}
