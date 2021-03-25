package com.david.order.service.impl;

import com.david.order.entity.OrderInfo;
import com.david.order.mapper.OrderInfoMapper;
import com.david.order.service.OrderService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Administrator
 * @date 2021年03月25日11:12
 * @description
 */
@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    @Autowired
    OrderInfoMapper orderInfoMapper;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Override
    public Boolean createOrder() {
        //创建订单
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setOrderId(1);
        orderInfoMapper.insert(orderInfo);
//        throw new RuntimeException("手动抛异常");
        return true;
    }
}
