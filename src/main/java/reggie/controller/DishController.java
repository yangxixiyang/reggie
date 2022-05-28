package reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reggie.dto.DishDto;
import reggie.pojo.Category;
import reggie.pojo.Dish;
import reggie.pojo.R;
import reggie.service.CategoryService;
import reggie.service.DishFlavoeService;
import reggie.service.DishService;

import java.util.List;
import java.util.stream.Collectors;

@RequestMapping("/dish")
@RestController
public class DishController {
    @Autowired
    DishService dishService;
    @Autowired
    DishFlavoeService dishFlavoeService;
    @Autowired
    CategoryService categoryService;

    /**
     * 新增菜品，如果前端传回来的数据是json格式则需要用RequestBody注解反序列化到对象当中
     * @param dishDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto) {
        //涉及到两张表的操作单独在service层写方法
        dishService.saveWithFlavor(dishDto);
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
        Page<DishDto> dishdtopage=new Page();
        Page<Dish> dishPage = new Page<>();
        //如果没用搜索功能就不用模糊查询
        qw.like(name!=null,Dish::getName,name);
        qw.orderByDesc(Dish::getUpdateTime);
        dishService.page(dishPage,qw);
        //对象拷贝把dishpage里的数据除了records（之前查到的不全的列表）拷贝到DishDto里完整的查
        BeanUtils.copyProperties(dishPage,dishdtopage,"records");
        List<Dish> records = dishPage.getRecords();

//        List<DishDto> list=null;
//        for(Dish temp:records){
//            DishDto dishDto = new DishDto();
//            Long categoryId = temp.getCategoryId();
//            Category category = categoryService.getById(categoryId);
//            String categoryName = category.getName();
//            dishDto.setCategoryName(categoryName);
//
//        }
        List<DishDto> list = records.stream().map((item) -> {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item, dishDto);
            Long categoryId = item.getCategoryId();//获得分类id
            Category category = categoryService.getById(categoryId);
            String categoryName = category.getName();
            dishDto.setCategoryName(categoryName);
            return dishDto;
        }).collect(Collectors.toList());
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
        return R.success("修改菜品成功");
    }

    /**
     * 直接用dish对象来接受传过来的id参数，这样不管传过来的是id还是name还是其他值，都可以接受通用
     * 根据条件查对应的菜品
     * @param dish
     * @return
     */
    @GetMapping("/list")
    public R<List<Dish>> list(Dish dish){
        LambdaQueryWrapper<Dish> qw=new LambdaQueryWrapper<>();
        qw.eq(dish.getCategoryId()!=null,Dish::getCategoryId,dish.getCategoryId());
        qw.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
        //查起售的，禁售的不查
        qw.eq(Dish::getStatus,1);
        List<Dish> list = dishService.list(qw);
        return R.success(list);
    }

}
