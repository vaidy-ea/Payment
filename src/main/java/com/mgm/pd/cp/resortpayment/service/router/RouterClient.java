package com.mgm.pd.cp.resortpayment.service.router;

import com.mgm.pd.cp.resortpayment.dto.router.RouterRequest;
import com.mgm.pd.cp.resortpayment.dto.router.RouterResponseJson;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * This is mainly responsible for sending request to Intelligent Router(IR)
 */
@FeignClient(url = "${intelligent-router-service.url}", name = "${intelligent-router-service.name}")
public interface RouterClient {

    /**
     *
     * @param routerRequest: Requested to is converted and sent to IR
     * @return RouterResponseJson
     */
    @PostMapping(value = "/route")
    RouterResponseJson sendRequest(RouterRequest routerRequest);
}
