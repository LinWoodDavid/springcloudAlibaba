package com.david.account.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "account-server")
public interface AccountServiceClient {

    @GetMapping("/account/name")
    String name();

}
