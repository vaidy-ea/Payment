package com.mgm.pd.cp.resortpayment.service;

import com.mgm.pd.cp.resortpayment.dto.router.RouterRequest;
import com.mgm.pd.cp.resortpayment.dto.router.RouterResponseJson;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(url = "${intelligent-router-service.url}", name = "${intelligent-router-service.name}")
public interface RouterClient {
    @PostMapping(value = "/route")
    RouterResponseJson sendRequest(RouterRequest routerRequest);
}
