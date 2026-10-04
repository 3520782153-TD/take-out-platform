package com.it.reggie.dto;

import com.it.reggie.entity.OrderDetail;
import com.it.reggie.entity.Orders;
import lombok.Data;

import java.util.List;

@Data
public class OrderDto extends Orders {

    private List<OrderDetail> orderDetails;
}
