package com.david.account.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("account")
@RestController
@RefreshScope
public class AccountService {

    @Value("${config.name:姓名}")
    private String name;

    @GetMapping("/name")
    public String name() {
        return name;
    }

}
