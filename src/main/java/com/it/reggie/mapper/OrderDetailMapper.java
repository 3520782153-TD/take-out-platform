package com.it.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.it.reggie.entity.OrderDetail;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderDetailMapper extends BaseMapper<OrderDetail> {
}
