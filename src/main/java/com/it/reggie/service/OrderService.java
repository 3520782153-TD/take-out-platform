package com.it.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.it.reggie.dto.OrderDto;
import com.it.reggie.entity.Orders;

public interface OrderService extends IService<Orders> {

    /**
     * 用户下单
     * @param orders
     */
    public void submit(Orders orders);

    /**
     * 查询订单和明细
     */
    public OrderDto getByIdWithDetail(Long id);

    /**
     * 再来一单
     */
    public void again(Long id);
}
