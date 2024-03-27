package com.mgm.pd.cp.resortpayment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableFeignClients
@EntityScan("com.mgm.pd.cp.payment.common.model")
@ComponentScan(basePackages = {"com.mgm.pd.cp.payment.common","com.mgm.pd.cp.resortpayment"})
public class CPPaymentProcessing {

    public static void main(String[] args) {
        SpringApplication.run(CPPaymentProcessing.class, args);
    }
}
