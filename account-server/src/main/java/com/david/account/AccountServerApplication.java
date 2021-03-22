package com.david.account;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.david")
@ComponentScan({
        "com.david",
})
public class AccountServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(AccountServerApplication.class, args);
    }

}
