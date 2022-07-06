package reggie.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reggie.Util.CustomException;
import reggie.dto.SetmealDto;
import reggie.mapper.SetmealMapper;
import reggie.pojo.Setmeal;
import reggie.pojo.SetmealDish;

import java.util.List;

@Service
@Slf4j
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {
    @Autowired
    SetmealDishService setmealDishService;

    /**
     * 新增套餐。操作两张表，所以加上事务注解
     * @param setmealDto
     */
    @Override
    @Transactional
    public void saveWithDish(SetmealDto setmealDto) {
        //保存套餐基本信息
        this.save(setmealDto);
        //保存套餐和菜品的关联信息
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        //SetmealDish里没有关联的id值，老样子遍历赋值
        Long id = setmealDto.getId();
        for(SetmealDish temp:setmealDishes){
            temp.setSetmealId(id);
        }
        setmealDishService.saveBatch(setmealDishes);
    }

    /**
     * 删除套餐。多表删除
     * @param ids
     */
    @Override
    @Transactional
    public void removeWithDish(List<Long> ids) {
        //先查询套餐状态是否可以删除
        LambdaQueryWrapper<Setmeal> qw=new LambdaQueryWrapper<>();
        qw.in(Setmeal::getId,ids);
        qw.eq(Setmeal::getStatus,1);
        int count = this.count(qw);
        if (count>0){
            //不能删除抛出业务异常
            throw new CustomException("套餐正在售卖不能删除");
        }
        //如果可以,先删本表
        this.removeByIds(ids);
        //再删第三方表
        LambdaQueryWrapper<SetmealDish> qw1=new LambdaQueryWrapper<>();
        qw1.in(SetmealDish::getSetmealId,ids);
        setmealDishService.remove(qw1);
    }

    @Override
    public void updateWithDish(SetmealDto setmealDto) {
        this.updateById(setmealDto);
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        boolean b = setmealDishService.updateBatchById(setmealDishes);
    }
}
