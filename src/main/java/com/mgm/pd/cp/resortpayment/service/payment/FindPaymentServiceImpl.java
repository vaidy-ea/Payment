package com.mgm.pd.cp.resortpayment.service.payment;

import com.mgm.pd.cp.payment.common.constant.AuthType;
import com.mgm.pd.cp.payment.common.model.Payment;
import com.mgm.pd.cp.resortpayment.repository.PaymentRepository;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class FindPaymentServiceImpl implements FindPaymentService {
    private static final Logger logger = LogManager.getLogger(FindPaymentServiceImpl.class);
    private PaymentRepository paymentRepository;

    @Override
    @Retry(name = "initialAuthPayment")
    public Optional<List<Payment>> getPaymentDetails(Long authChainId, @Valid AuthType transactionType) {
        logger.log(Level.DEBUG, "Attempting to find Initial Auth from Payment DB using authChainId: " + authChainId);
        return paymentRepository.findByAuthChainIdAndAuthSubType(authChainId, transactionType);
    }

    @Override
    @Retry(name = "initialAuthPaymentForVoid")
    public Optional<List<Payment>> getPaymentDetails(Long authChainId) {
        logger.log(Level.DEBUG, "Attempting to find Initial Auth from Payment DB using authChainId: " + authChainId);
        return paymentRepository.findByAuthChainId(authChainId);
    }
}
