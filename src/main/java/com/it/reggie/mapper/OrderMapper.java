package com.it.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.it.reggie.entity.Orders;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderMapper extends BaseMapper<Orders> {
}
