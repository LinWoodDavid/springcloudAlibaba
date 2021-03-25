package com.david.order.service;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Administrator
 * @date 2021年03月25日11:11
 * @description 订单业务处理
 */
@RestController
public interface OrderService {

    @RequestMapping("createOrder")
    Boolean createOrder();

}
