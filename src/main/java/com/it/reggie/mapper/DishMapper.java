package com.it.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.it.reggie.entity.Dish;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DishMapper extends BaseMapper<Dish> {

    //查询菜品关联的套餐数量
    @Select({"<script>",
            "select count(*) from setmeal_dish where is_deleted = 0 and dish_id in",
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>",
            "cast(#{id} as char)",
            "</foreach>",
            "</script>"})
    public int countSetmealByDishIds(@Param("ids") List<Long> ids);
}
