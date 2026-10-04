package com.it.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.it.reggie.common.CustomException;
import com.it.reggie.dto.SetmealDto;
import com.it.reggie.entity.Setmeal;
import com.it.reggie.entity.SetmealDish;
import com.it.reggie.mapper.SetmealMapper;
import com.it.reggie.service.SetmealDishService;
import com.it.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper,Setmeal> implements SetmealService {
    @Autowired
    private SetmealDishService setmealDishService;
    /**
     * 新增套餐，同时需要保存套餐和菜品的关联关系
     * @param setmealDto
     */
    @Transactional
    public void saveWithDish(SetmealDto setmealDto){
        //保存套餐基本信息，操作setmeal，执行insert操作
        this.save(setmealDto);

        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        setmealDishes.stream().map((item)->{
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());
        //保存套餐和菜品的关联信息，操作setmeal_dish,执行insert操作
        setmealDishService.saveBatch(setmealDishes);
    }
    /**
     * 删除套餐，同时需要删除套餐和菜品的关联数据
     * @param ids
     */
    @Override
    @Transactional
    public void removeWithDish(List<Long> ids){
        if(ids==null || ids.isEmpty()){
            throw new CustomException("请选择需要删除的套餐");
        }

        //查询套餐状态，确定是否可以删除
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.in(Setmeal::getId,ids);
        int total = this.count(queryWrapper);
        if(total!=ids.stream().distinct().count()){
            throw new CustomException("套餐不存在");
        }
        queryWrapper.eq(Setmeal::getStatus,1);

        int count = this.count(queryWrapper);
        if(count>0){
            //如果不能删除，抛出一个业务异常
            throw new CustomException("套餐正在售卖中，不能删除");
        }
        //如果可以删除，先删除套餐表中数据——setmeal
        boolean result = this.removeByIds(ids);
        if(!result){
            throw new CustomException("套餐不存在");
        }


        //delete form setmeal_dish where setmeal_id in(1,2,3)
        LambdaQueryWrapper<SetmealDish> lambdaQueryWrapper = new LambdaQueryWrapper();
        List<String> setmealIds = ids.stream().map(String::valueOf).collect(Collectors.toList());
        lambdaQueryWrapper.in(SetmealDish::getSetmealId,setmealIds);
        //删除关系表中数据——setmeal_dish
        setmealDishService.remove(lambdaQueryWrapper);
    }

    /**
     * 根据id查询套餐信息和对应菜品信息
     * @param id
     * @return
     */
    @Override
    public SetmealDto getByIdWithDish(Long id){
        Setmeal setmeal = this.getById(id);
        if(setmeal==null){
            throw new CustomException("套餐不存在");
        }
        SetmealDto setmealDto = new SetmealDto();
        BeanUtils.copyProperties(setmeal,setmealDto);

        //查询当前套餐对应的菜品信息
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId,String.valueOf(id));
        queryWrapper.orderByAsc(SetmealDish::getSort).orderByAsc(SetmealDish::getId);
        List<SetmealDish> setmealDishes = setmealDishService.list(queryWrapper);
        setmealDto.setSetmealDishes(setmealDishes);

        return setmealDto;
    }

    /**
     * 修改套餐，同时更新套餐和菜品的关联关系
     * @param setmealDto
     */
    @Override
    @Transactional
    public void updateWithDish(SetmealDto setmealDto){
        if(setmealDto.getId()==null || this.getById(setmealDto.getId())==null){
            throw new CustomException("套餐不存在");
        }
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        if(setmealDishes==null || setmealDishes.isEmpty()){
            throw new CustomException("请选择套餐菜品");
        }

        //更新套餐基本信息
        boolean result = this.updateById(setmealDto);
        if(!result){
            throw new CustomException("套餐不存在");
        }

        //清理当前套餐对应的菜品关联数据
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId,String.valueOf(setmealDto.getId()));
        setmealDishService.remove(queryWrapper);

        //添加当前提交的菜品关联数据
        setmealDishes.stream().map((item)->{
            item.setId(null);
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());
        setmealDishService.saveBatch(setmealDishes);
    }
}
