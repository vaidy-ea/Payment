package com.mgm.pd.cp.resortpayment.service.payment;

import com.mgm.pd.cp.resortpayment.model.Payment;
import com.mgm.pd.cp.resortpayment.repository.PaymentRepository;
import io.github.resilience4j.retry.annotation.Retry;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class FindPaymentServiceImpl implements FindPaymentService {
    private static final Logger logger = LogManager.getLogger(FindPaymentServiceImpl.class);
    @Autowired
    PaymentRepository paymentRepository;
    @Override
    @Retry(name = "initialAuthPayment")
    public Optional<Payment> getPaymentDetails(String resortId, String reservationNumber) {
        logger.log(Level.DEBUG, "Attempting to find Initial Auth from Payment DB using resortId: " + resortId);
        return paymentRepository.findFirstByPropertyCodeAndResvNameIDAndAuthTypeNotNullAndApprovalCodeNotNullOrderByIdDesc(resortId, reservationNumber);
    }
}
