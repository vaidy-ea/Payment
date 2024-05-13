package com.mgm.pd.cp.resortpayment.config;

import com.mgm.pd.cp.resortpayment.service.router.RouterClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;

@SpringBootTest
class RouterHealthIndicatorTest {
    @Autowired
    RouterHealthIndicator routerHealthIndicator;

    @MockBean
    private RouterClient client;

    @Test
    void test_router_health_ok(){
        //given
        Mockito.when(client.getHealth()).thenReturn(ResponseEntity.ok("ok"));
        Health health = routerHealthIndicator.health();

        Assertions.assertEquals("UP", health.getStatus().getCode());
    }

    @Test
    void test_router_health_down(){
        //given
        Mockito.when(client.getHealth()).thenReturn(ResponseEntity.badRequest().body("down"));
        Health health = routerHealthIndicator.health();

        Assertions.assertEquals("DOWN", health.getStatus().getCode());
    }
}
