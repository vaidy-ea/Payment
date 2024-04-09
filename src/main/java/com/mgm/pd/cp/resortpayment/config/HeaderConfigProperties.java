package com.mgm.pd.cp.resortpayment.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "mgm.cp.payment.header-config")
@Getter
@Setter
public class HeaderConfigProperties {
    private List<String> requiredHeaders;
}
