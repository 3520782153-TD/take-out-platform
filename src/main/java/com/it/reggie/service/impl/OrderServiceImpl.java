package com.it.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.it.reggie.common.BaseContext;
import com.it.reggie.common.CustomException;
import com.it.reggie.dto.OrderDto;
import com.it.reggie.entity.*;
import com.it.reggie.mapper.OrderMapper;
import com.it.reggie.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Orders> implements OrderService {

    @Autowired
    private ShoppingCartService shoppingCartService;

    @Autowired
    private UserService userService;

    @Autowired
    private AddressBookService addressBookService;

    @Autowired
    private OrderDetailService orderDetailService;

    @Autowired
    private DishService dishService;

    @Autowired
    private SetmealService setmealService;

    /**
     * 用户下单
     * @param orders
     */
    @Transactional
    public void submit(Orders orders) {
        //获得当前用户id
        Long userId = BaseContext.getCurrentId();

        //查询当前用户的购物车数据
        LambdaQueryWrapper<ShoppingCart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShoppingCart::getUserId,userId);
        List<ShoppingCart> shoppingCarts = shoppingCartService.list(wrapper);

        if(shoppingCarts == null || shoppingCarts.size() == 0){
            throw new CustomException("购物车为空，不能下单");
        }

        //查询用户数据
        User user = userService.getById(userId);

        //查询地址数据
        Long addressBookId = orders.getAddressBookId();
        AddressBook addressBook = addressBookService.getById(addressBookId);
        if(addressBook == null || !userId.equals(addressBook.getUserId())){
            throw new CustomException("用户地址信息有误，不能下单");
        }

        long orderId = IdWorker.getId();//订单号

        BigDecimal amount = shoppingCarts.stream().map(item ->
                item.getAmount().multiply(new BigDecimal(item.getNumber())))
                .reduce(BigDecimal.ZERO,BigDecimal::add);

        List<OrderDetail> orderDetails = shoppingCarts.stream().map((item) -> {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrderId(orderId);
            orderDetail.setNumber(item.getNumber());
            orderDetail.setDishFlavor(item.getDishFlavor());
            orderDetail.setDishId(item.getDishId());
            orderDetail.setSetmealId(item.getSetmealId());
            orderDetail.setName(item.getName());
            orderDetail.setImage(item.getImage());
            orderDetail.setAmount(item.getAmount());
            return orderDetail;
        }).collect(Collectors.toList());


        orders.setId(orderId);
        orders.setOrderTime(LocalDateTime.now());
        orders.setCheckoutTime(LocalDateTime.now());
        orders.setStatus(2);
        orders.setAmount(amount);//总金额
        orders.setUserId(userId);
        orders.setNumber(String.valueOf(orderId));
        orders.setUserName(user.getName() == null || user.getName().isEmpty() ? addressBook.getConsignee() : user.getName());
        orders.setConsignee(addressBook.getConsignee());
        orders.setPhone(addressBook.getPhone());
        orders.setAddress((addressBook.getProvinceName() == null ? "" : addressBook.getProvinceName())
                + (addressBook.getCityName() == null ? "" : addressBook.getCityName())
                + (addressBook.getDistrictName() == null ? "" : addressBook.getDistrictName())
                + (addressBook.getDetail() == null ? "" : addressBook.getDetail()));
        //向订单表插入数据，一条数据
        this.save(orders);

        //向订单明细表插入数据，多条数据
        orderDetailService.saveBatch(orderDetails);

        //清空购物车数据
        shoppingCartService.remove(wrapper);
    }

    /**
     * 查询订单和明细
     */
    public OrderDto getByIdWithDetail(Long id){
        Orders orders = this.getById(id);
        if(orders == null){
            return null;
        }
        OrderDto orderDto = new OrderDto();
        BeanUtils.copyProperties(orders,orderDto);
        LambdaQueryWrapper<OrderDetail> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderDetail::getOrderId,id);
        queryWrapper.orderByAsc(OrderDetail::getId);
        orderDto.setOrderDetails(orderDetailService.list(queryWrapper));
        return orderDto;
    }

    /**
     * 将已完成订单的商品加入当前用户购物车
     */
    @Transactional
    public void again(Long id){
        OrderDto orderDto = this.getByIdWithDetail(id);
        Long userId = BaseContext.getCurrentId();
        if(orderDto == null || !userId.equals(orderDto.getUserId())){
            throw new CustomException("订单不存在");
        }
        if(orderDto.getStatus() != 4 || orderDto.getOrderDetails().isEmpty()){
            throw new CustomException("只有已完成的订单可以再来一单");
        }
        for(OrderDetail item : orderDto.getOrderDetails()){
            ShoppingCart shoppingCart = new ShoppingCart();
            BeanUtils.copyProperties(item,shoppingCart,"id");
            shoppingCart.setUserId(userId);
            shoppingCart.setCreateTime(LocalDateTime.now());
            if(item.getDishId() != null){
                Dish dish = dishService.getById(item.getDishId());
                if(dish == null || dish.getStatus() != 1){
                    throw new CustomException("订单中的菜品已停售或删除");
                }
                shoppingCart.setName(dish.getName());
                shoppingCart.setImage(dish.getImage());
                shoppingCart.setAmount(dish.getPrice().divide(new BigDecimal(100)));
            }else{
                Setmeal setmeal = setmealService.getById(item.getSetmealId());
                if(setmeal == null || setmeal.getStatus() != 1){
                    throw new CustomException("订单中的套餐已停售或删除");
                }
                shoppingCart.setName(setmeal.getName());
                shoppingCart.setImage(setmeal.getImage());
                shoppingCart.setAmount(setmeal.getPrice().divide(new BigDecimal(100)));
            }
            LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ShoppingCart::getUserId,userId);
            queryWrapper.eq(item.getDishId() != null,ShoppingCart::getDishId,item.getDishId());
            queryWrapper.eq(item.getSetmealId() != null,ShoppingCart::getSetmealId,item.getSetmealId());
            if(item.getDishFlavor() == null){
                queryWrapper.isNull(ShoppingCart::getDishFlavor);
            }else{
                queryWrapper.eq(ShoppingCart::getDishFlavor,item.getDishFlavor());
            }
            ShoppingCart cart = shoppingCartService.getOne(queryWrapper);
            if(cart == null){
                shoppingCartService.save(shoppingCart);
            }else{
                cart.setNumber(cart.getNumber() + item.getNumber());
                cart.setAmount(shoppingCart.getAmount());
                cart.setName(shoppingCart.getName());
                cart.setImage(shoppingCart.getImage());
                shoppingCartService.updateById(cart);
            }
        }
    }
}
