package com.david.account.controller;

import com.david.account.client.AccountServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("account")
@RestController
public class AccountController {

    @Autowired
    AccountServiceClient accountServiceClient;

    @GetMapping("/name")
    public String name() {
        return accountServiceClient.name();
    }

}
