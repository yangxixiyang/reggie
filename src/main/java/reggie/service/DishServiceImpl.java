package reggie.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
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
        //存错完后拿到自动生成的菜品id值
        Long id = dishDto.getId();
        //给菜品口味存
        List<DishFlavor> flavors = dishDto.getFlavors();
        for(DishFlavor temp : flavors){
            temp.setDishId(id);
        }
        dishFlavoeService.saveBatch(flavors);
    }


}
