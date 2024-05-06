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
    @Retry(name = "authChainIdAndAuthSubType")
    public Optional<List<Payment>> getPaymentDetails(String authChainId, @Valid AuthType authSubType) {
        logger.log(Level.DEBUG, "Attempting to find Initial Auth from Payment DB using authChainId: {} and authSubType: {}", authChainId, authSubType);
        return paymentRepository.findByAuthChainIdAndAuthSubType(authChainId, authSubType);
    }

    @Override
    @Retry(name = "authChainId")
    public Optional<List<Payment>> getPaymentDetails(String authChainId) {
        logger.log(Level.DEBUG, "Attempting to find Initial Auth from Payment DB using authChainId: {}", authChainId);
        return paymentRepository.findByAuthChainIdOrderByUpdatedTimestampDesc(authChainId);
    }

    @Override
    @Retry(name = "paymentAuthIdAndReferenceId")
    public Optional<List<Payment>> getPaymentDetailsByApprovalCode(String paymentAuthId) {
        logger.log(Level.DEBUG, "Attempting to find Initial Auth Payments from Payment DB using paymentAuthId: {}", paymentAuthId);
        /*
         * this will return payments which belongs to Transaction_Type:
         * 1. Authorize - Initial Auth/Incremental Auth
         * 2. Refund as for both cases ReferenceId can be null
         */
        return paymentRepository.findByPaymentAuthIdAndReferenceIdIsNullOrderByUpdatedTimestampDesc(paymentAuthId);
    }

    @Override
    @Retry(name = "mgmTransactionId")
    public Optional<List<Payment>> getPaymentDetailsByTransactionId(String transactionId) {
        logger.log(Level.DEBUG, "Attempting to find Payment using transactionId: {} ", transactionId);
        return paymentRepository.findByMgmTransactionId(transactionId);
    }
}
