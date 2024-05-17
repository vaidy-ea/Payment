package com.mgm.pd.cp.resortpayment.config;

import com.mgm.pd.cp.resortpayment.service.router.RouterClient;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component("intelligentRouter")
@AllArgsConstructor
public class RouterHealthIndicator implements HealthIndicator {
    private static final Logger logger = LogManager.getLogger(RouterHealthIndicator.class);
    private RouterClient client;

    @Override
    public Health health() {
        ResponseEntity<String> response = client.getHealth();
        Health.Builder status;
        if(response.getStatusCode().value() == 200){
                status = Health.up();
        } else {
            logger.log(Level.ERROR, "Router Health Indicator is down : {} ", response);
            status = Health.down();
        }
        return status.build();
    }
}
