package com.mgm.pd.cp.resortpayment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
@EntityScan("com.mgm.pd.cp.payment.common")
public class CPPaymentProcessing {

    public static void main(String[] args) {
        SpringApplication.run(CPPaymentProcessing.class, args);
    }
}
