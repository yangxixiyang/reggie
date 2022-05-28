package reggie.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reggie.dto.DishDto;
import reggie.mapper.DishMapper;
import reggie.pojo.Dish;
import reggie.pojo.DishFlavor;

import java.util.List;

@Service
@Slf4j
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {
    @Autowired
    DishFlavoeService dishFlavoeService;

    /**
     *新增菜品
     * @param dishDto
     */
    @Override
    @Transactional//事务控制注解
    public void saveWithFlavor(DishDto dishDto) {
        //保存菜品的相关信息到菜品表,dishdto继承至Dish类，能映射的属性就会自动映射存储
        this.save(dishDto);
        //存完后拿到自动生成的菜品id值
        Long id = dishDto.getId();
        //给菜品口味存
        List<DishFlavor> flavors = dishDto.getFlavors();
        //给菜品口味赋值id
        for(DishFlavor temp : flavors){
            temp.setDishId(id);
        }
        dishFlavoeService.saveBatch(flavors);
    }

    /**
     * 根据id查询菜品信息和对应的口味信息
     * @param id
     * @return
     */
    @Override
    public DishDto getByIdWithFlavor(long id) {
        //查询菜品
        Dish dish = this.getById(id);

        //查询当前菜品口味,从dishflavor表中查
        LambdaQueryWrapper<DishFlavor> wq=new LambdaQueryWrapper<>();
        LambdaQueryWrapper<DishFlavor> dishFlavorLambdaQueryWrapper = wq.eq(DishFlavor::getDishId, dish.getId());
        List<DishFlavor> list = dishFlavoeService.list(dishFlavorLambdaQueryWrapper);

        //创建dto对象并把dish里的基本信息赋值给dto
        DishDto dishDto=new DishDto();
        BeanUtils.copyProperties(dish,dishDto);

        //给dishDto设置口味的值
        dishDto.setFlavors(list);
        return dishDto;
    }

    /**
     * 修改菜品信息，菜品表和口味表
     * @param dishDto
     */
    @Override
    @Transactional
    public void updateWithFlavor(DishDto dishDto) {
        //先修改菜品的基本信息
        this.updateById(dishDto);
        //删除菜品的口味信息
        LambdaQueryWrapper<DishFlavor> qw=new LambdaQueryWrapper<>();
        LambdaQueryWrapper<DishFlavor> dishFlavorLambdaQueryWrapper = qw.eq(DishFlavor::getDishId, dishDto.getId());
        dishFlavoeService.remove(dishFlavorLambdaQueryWrapper);
        //然后增加菜品的口味信息
        List<DishFlavor> flavorList = dishDto.getFlavors();
        //菜品口味里没有菜品id值，手动赋值后再存,dish基本信息存到表里自动生成了id值
        Long id = dishDto.getId();
        for (DishFlavor temp:flavorList){
            temp.setDishId(id);
        }
        dishFlavoeService.saveBatch(flavorList);
    }


}
