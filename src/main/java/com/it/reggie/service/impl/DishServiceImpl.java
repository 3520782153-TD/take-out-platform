package com.it.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.it.reggie.common.CustomException;
import com.it.reggie.dto.DishDto;
import com.it.reggie.entity.Dish;
import com.it.reggie.entity.DishFlavor;
import com.it.reggie.mapper.DishFlavorMapper;
import com.it.reggie.mapper.DishMapper;
import com.it.reggie.service.DishFlavorService;
import com.it.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.Arrays.stream;

@Service
@Slf4j
public class DishServiceImpl extends ServiceImpl<DishMapper,Dish> implements DishService   {
    @Autowired
    private DishFlavorService dishFlavorService;
    /**
     * 新增菜品，同时保存对应口味数据
     * @param dishDto
     */

    @Transactional
    public void saveWithFlavor(DishDto dishDto) {
        //保存菜品基本信息到菜品表dish
        this.save(dishDto);

        Long dishId = dishDto.getId();//菜品id

        //菜品口味
        List<DishFlavor> flavors = dishDto.getFlavors();
        flavors = flavors.stream().map((item)->{
            item.setDishId(dishId);
            return item;
        }).collect(Collectors.toList());

        //保存菜品口味数据到菜品口味表dish_flavor
        dishFlavorService.saveBatch(flavors);
    }

    /**
     * 根据id查询菜品信息和对应口味信息
     * @param id
     * @return
     */
    public DishDto getByIdWithFlavor(Long id) {
        //查询菜品基本信息，从dish表查询
        Dish dish = this.getById(id);
        DishDto dishDto = new DishDto();
        BeanUtils.copyProperties(dish,dishDto);
        //查询当前菜品对应口味信息，从dish_flavor表查询
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId, dish.getId());
        List<DishFlavor> flavors = dishFlavorService.list(queryWrapper);
        dishDto.setFlavors(flavors);

        return dishDto;
    }
    @Override
    @Transactional
    public void updateWithFlavor(DishDto dishDto){
        //更新dish表基本信息
        this.updateById(dishDto);
        //清理当前菜品对应口味数据——dish_flavor表的delete操作
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId, dishDto.getId());

        dishFlavorService.remove(queryWrapper);
        //添加当前提交过来的口味数据——dish_flavor表的insert操作
        List<DishFlavor> flavors = dishDto.getFlavors();

        flavors = flavors.stream().map((item)->{
            item.setDishId(dishDto.getId());
            return item;
        }).collect(Collectors.toList());

        dishFlavorService.saveBatch(flavors);

    }

    /**
     * 删除菜品，同时删除对应口味数据
     * @param ids
     */
    @Override
    @Transactional
    public void removeWithFlavor(List<Long> ids){
        if(ids==null || ids.isEmpty()){
            throw new CustomException("请选择需要删除的菜品");
        }

        //查询菜品是否启售，启售中的菜品不能删除
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Dish::getId,ids);
        queryWrapper.eq(Dish::getStatus,1);
        int count = this.count(queryWrapper);
        if(count>0){
            throw new CustomException("菜品正在启售，不能删除，请先停售");
        }

        //查询菜品是否关联套餐，已经关联的菜品不能删除
        int setmealCount = this.baseMapper.countSetmealByDishIds(ids);
        if(setmealCount>0){
            throw new CustomException("菜品关联了套餐，不能删除");
        }

        //删除当前菜品对应口味数据
        LambdaQueryWrapper<DishFlavor> flavorQueryWrapper = new LambdaQueryWrapper<>();
        flavorQueryWrapper.in(DishFlavor::getDishId,ids);
        dishFlavorService.remove(flavorQueryWrapper);

        //删除菜品基本信息
        boolean result = this.removeByIds(ids);
        if(!result){
            throw new CustomException("菜品不存在");
        }
    }
}
