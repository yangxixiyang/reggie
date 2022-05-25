package reggie.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reggie.Util.CustomException;
import reggie.mapper.CategoryMapper;
import reggie.mapper.DishMapper;
import reggie.pojo.Category;
import reggie.pojo.Dish;
import reggie.pojo.Setmeal;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService{
    @Autowired
    DishService dishService;
    @Autowired
    SetmealService setmealService;
    /**
     * 根据id删除分类，先判断是否关联了菜品或套餐
     * @param id
     */
    @Override
    public void remove(Long id) {
        //查询菜品里还有没有属于这个分类的,添加查询条件
        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper=new LambdaQueryWrapper<>();
        dishLambdaQueryWrapper.eq(Dish::getCategoryId,id);
        int count = dishService.count(dishLambdaQueryWrapper);
        //如果还包含菜品，抛出自定义的异常，给异常捕获器来处理
        if (count > 0) {
            throw new CustomException("当前分类关联了菜品，不能直接删除");
        }
        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper=new LambdaQueryWrapper<>();
        setmealLambdaQueryWrapper.eq(Setmeal::getCategoryId,id);
        int count1 = setmealService.count(setmealLambdaQueryWrapper);
        if (count1 > 0) {
            throw new CustomException("当前分类关联了套餐，不能直接删除");
        }
        //如果都没有关联，直接调用父类方法删除
        super.removeById(id);
    }
}
