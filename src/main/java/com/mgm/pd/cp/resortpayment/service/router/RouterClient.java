package com.mgm.pd.cp.resortpayment.service.router;

import com.mgm.pd.cp.resortpayment.dto.router.RouterRequest;
import com.mgm.pd.cp.resortpayment.dto.router.RouterResponseJson;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * This is mainly responsible for sending request to Intelligent Router(IR)
 */
@FeignClient(url = "${intelligent-router-service.url}", name = "${intelligent-router-service.name}")
public interface RouterClient {

    /**
     *
     * @return RouterResponseJson
     */
    @GetMapping(value = "/actuator/health")
    ResponseEntity<String> getHealth();

  /**
     *
     * @param routerRequest: Requested to is converted and sent to IR
     * @return RouterResponseJson
     */
    @PostMapping(value = "/routerservice/v1/route")
    RouterResponseJson sendRequest(@RequestHeader HttpHeaders headers, RouterRequest routerRequest);
}
