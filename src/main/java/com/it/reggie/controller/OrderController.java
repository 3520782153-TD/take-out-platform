package com.it.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.it.reggie.common.R;
import com.it.reggie.dto.OrderDto;
import com.it.reggie.entity.Orders;
import com.it.reggie.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.BeanUtils;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 订单
 */
@Slf4j
@RestController
@RequestMapping("/order")
public class OrderController {
    @Autowired
    private OrderService orderService;

    /**
     * 用户下单
     * @param orders
     * @return
     */
    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders){
        orderService.submit(orders);
        return R.success("下单成功");
    }

    /**
     * 管理端订单分页查询
     */
    @GetMapping("/page")
    public R<Page> page(int page,int pageSize,String number,
                        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime beginTime,
                        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,HttpSession session){
        if(session.getAttribute("employee") == null){
            return R.error("NOTLOGIN");
        }
        Page<Orders> pageInfo = new Page<>(page,pageSize);
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.hasText(number),Orders::getNumber,number);
        queryWrapper.ge(beginTime != null,Orders::getOrderTime,beginTime);
        queryWrapper.le(endTime != null,Orders::getOrderTime,endTime);
        queryWrapper.orderByDesc(Orders::getOrderTime,Orders::getId);
        orderService.page(pageInfo,queryWrapper);
        return R.success(pageInfo);
    }

    /**
     * 当前用户订单分页查询，包含订单明细
     */
    @GetMapping("/userPage")
    public R<Page> userPage(int page,int pageSize,HttpSession session){
        Long userId = (Long) session.getAttribute("user");
        if(userId == null){
            return R.error("NOTLOGIN");
        }
        Page<Orders> pageInfo = new Page<>(page,pageSize);
        Page<OrderDto> dtoPage = new Page<>();
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Orders::getUserId,userId);
        queryWrapper.orderByDesc(Orders::getOrderTime,Orders::getId);
        orderService.page(pageInfo,queryWrapper);
        BeanUtils.copyProperties(pageInfo,dtoPage,"records");
        List<OrderDto> list = pageInfo.getRecords().stream().map(item ->
                orderService.getByIdWithDetail(item.getId())).collect(Collectors.toList());
        dtoPage.setRecords(list);
        return R.success(dtoPage);
    }

    /**
     * 派送、完成订单
     */
    @PutMapping
    public R<String> update(@RequestBody Orders orders,HttpSession session){
        if(session.getAttribute("employee") == null){
            return R.error("NOTLOGIN");
        }
        if(orders.getId() == null || orders.getStatus() == null){
            return R.error("订单信息不完整");
        }
        Orders order = orderService.getById(orders.getId());
        if(order == null){
            return R.error("订单不存在");
        }
        int status = orders.getStatus();
        if(!(order.getStatus() == 2 && status == 3 || order.getStatus() == 3 && status == 4)){
            return R.error("订单状态不允许此操作");
        }
        Orders updateOrder = new Orders();
        updateOrder.setStatus(status);
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Orders::getId,order.getId());
        queryWrapper.eq(Orders::getStatus,order.getStatus());
        if(!orderService.update(updateOrder,queryWrapper)){
            return R.error("订单状态已变化，请刷新后重试");
        }
        return R.success("订单状态修改成功");
    }

    /**
     * 再来一单
     */
    @PostMapping("/again")
    public R<String> again(@RequestBody Orders orders){
        if(orders.getId() == null){
            return R.error("请选择订单");
        }
        orderService.again(orders.getId());
        return R.success("商品已加入购物车");
    }
}
