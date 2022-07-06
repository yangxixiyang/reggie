package reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sun.org.apache.bcel.internal.generic.NEW;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;
import reggie.Util.CustomException;
import reggie.dto.DishDto;
import reggie.pojo.Category;
import reggie.pojo.Dish;
import reggie.pojo.DishFlavor;
import reggie.pojo.R;
import reggie.service.CategoryService;
import reggie.service.DishFlavoeService;
import reggie.service.DishService;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RequestMapping("/dish")
@RestController
@Slf4j
public class DishController {
    @Autowired
    DishService dishService;
    @Autowired
    DishFlavoeService dishFlavoeService;
    @Autowired
    CategoryService categoryService;
    @Autowired
    RedisTemplate redisTemplate;

    /**
     * 新增菜品，如果前端传回来的数据是json格式则需要用RequestBody注解反序列化到对象当中
     * @param dishDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto) {
        //涉及到两张表的操作单独在service层写方法
        dishService.saveWithFlavor(dishDto);
        //修改菜品清除缓存(不用全部清除，精确清除修改的那一个分类)
        String key = "dish_"+ dishDto.getCategoryId() + "_" + dishDto.getStatus();
        redisTemplate.delete(key);
        return R.success("新增菜品成功");
    }

    /**
     * 菜品分页查询，需要多表查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page,int pageSize,String name){
        LambdaQueryWrapper<Dish> qw=new LambdaQueryWrapper();
        Page<DishDto> dishdtopage=new Page(page,pageSize);
        Page<Dish> dishPage = new Page<>(page,pageSize);
        //如果没用搜索功能就不用模糊查询
        qw.like(name!=null,Dish::getName,name);
        qw.orderByDesc(Dish::getUpdateTime);
        dishService.page(dishPage,qw);
        //对象拷贝把dishpage里的数据分页大小数量除了records（之前查到的不全的列表）拷贝到DishDto里
        BeanUtils.copyProperties(dishPage,dishdtopage,"records");
        List<Dish> records = dishPage.getRecords();

        List<DishDto> list = new ArrayList<>();
        for(Dish temp:records){
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(temp, dishDto);
            Long categoryId = temp.getCategoryId();
            Category category = categoryService.getById(categoryId);
            String categoryName = category.getName();
            dishDto.setCategoryName(categoryName);
            list.add(dishDto);
        }
        //上面是for循环方式实现遍历，下面是stream流方式实现遍历

//        List<DishDto> list = records.stream().map((item) -> {
//            DishDto dishDto = new DishDto();
//            BeanUtils.copyProperties(item, dishDto);
//            Long categoryId = item.getCategoryId();//获得分类id
//            Category category = categoryService.getById(categoryId);
//            String categoryName = category.getName();
//            dishDto.setCategoryName(categoryName);
//            return dishDto;
//        }).collect(Collectors.toList());
        dishdtopage.setRecords(list);
        return R.success(dishdtopage);
    }

    /**
     * 根据id查询菜品和口味信息.REST风格,需要多表查询，编写service，组合成Dto返回
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<DishDto> get(@PathVariable Long id){
        DishDto dishDto = dishService.getByIdWithFlavor(id);
        return R.success(dishDto);
    }

    /**
     * 修改菜品，两张表，扩展service方法
     * @param dishDto
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto) {
        dishService.updateWithFlavor(dishDto);
        //修改菜品清除缓存(不用全部清除，精确清除修改的那一个分类)
        String key = "dish_"+ dishDto.getCategoryId() + "_" + dishDto.getStatus();
        redisTemplate.delete(key);
        return R.success("修改菜品成功");
    }

    /**
     * 直接用dish对象来接受传过来的id参数，这样不管传过来的是id还是name还是其他值，都可以接受通用
     * 根据条件查对应的菜品
     * @param dish
     * @return
     */
    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish){
        List<DishDto> dishlist = null;
        //动态构造key
        String key ="dish_" + dish.getCategoryId() + "_" + dish.getStatus();
        //先从redis里取缓存数据
        dishlist = (List<DishDto>)redisTemplate.opsForValue().get(key);
        //如果有数据之间返回，没有数据执行下一步查数据库，查完数据库记得存到redis里
        if (dishlist != null){
            return R.success(dishlist);
        }
        LambdaQueryWrapper<Dish> qw=new LambdaQueryWrapper<>();
        qw.eq(dish.getCategoryId()!=null,Dish::getCategoryId,dish.getCategoryId());
        qw.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
        //查起售的，禁售的不查
        qw.eq(Dish::getStatus,1);
        List<Dish> list = dishService.list(qw);

        dishlist = list.stream().map((item) -> {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item, dishDto);
            Long categoryId = item.getCategoryId();//获得分类id
            Category category = categoryService.getById(categoryId);
            if(category!=null) {
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }
            Long dishid = item.getId();
            LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper=new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(DishFlavor::getDishId,dishid);
            List<DishFlavor> dishFlavorList = dishFlavoeService.list(lambdaQueryWrapper);
            dishDto.setFlavors(dishFlavorList);
            return dishDto;
        }).collect(Collectors.toList());
        //对象被序列化了所有能以字符串形式存到redis里
        redisTemplate.opsForValue().set(key,dishlist);
        return R.success(dishlist);
    }

    /**
     * 修改菜品售卖信息，REST风格，POST请求，还有批量修改所以id用LIST接受
     * @RequestParam是负责把请求参数映射到功能处理数据上（用于接受ids）
     * @PathVariable可以将URL中占位符参数绑定到控制器处理方法的入参中
     * @RequestBody用于接受json参数
     * @param ids
     * @return
     */
    @PostMapping("/status/{status}")
    public R<String> satatus(@PathVariable(value="status") Integer status,@RequestParam(value="ids") List<Long> ids){
        //把需要改的对象查出来
        List<Dish> dishes = dishService.listByIds(ids);
        //修改
        for (Dish dish: dishes) {
            dish.setStatus(status);
            dishService.updateById(dish);
        }
        return R.success("662.6");
     }

    /**
     * 删除菜品
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids){
        List<Dish> dishes = dishService.listByIds(ids);
        for (Dish dish:dishes){
            if (dish.getStatus() != 0){
                throw new CustomException("修改菜品前需要先禁售菜品");
            }
        }
        dishService.removeByIds(ids);
        //清除缓存(菜品批量删除类别会不一样，干脆全部清除)
        Set keys = redisTemplate.keys("dish_*");
        redisTemplate.delete(keys);
        return R.success("chen");
    }
}
