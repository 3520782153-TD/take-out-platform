package com.it.reggie.controller;

import com.it.reggie.common.R;
import com.it.reggie.entity.OrderDetail;
import com.it.reggie.dto.OrderDto;
import com.it.reggie.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * 订单明细
 */
@Slf4j
@RestController
@RequestMapping("/orderDetail")
public class OrderDetailController {
    @Autowired
    private OrderService orderService;

    /**
     * 根据订单id查询明细
     */
    @GetMapping("/{id}")
    public R<List<OrderDetail>> get(@PathVariable Long id,HttpSession session){
        OrderDto orderDto = orderService.getByIdWithDetail(id);
        if(orderDto == null){
            return R.error("订单不存在");
        }
        Long userId = (Long) session.getAttribute("user");
        if(session.getAttribute("employee") == null && !orderDto.getUserId().equals(userId)){
            return R.error("订单不存在");
        }
        return R.success(orderDto.getOrderDetails());
    }
}
