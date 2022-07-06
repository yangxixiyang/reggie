package reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;
import reggie.dto.SetmealDto;
import reggie.pojo.*;
import reggie.service.CategoryService;
import reggie.service.SetmealDishService;
import reggie.service.SetmealService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 套餐管理
 */
@RestController
@RequestMapping("/setmeal")
@Slf4j
public class SetmealController {
    @Autowired
    SetmealService setmealService;
    @Autowired
    SetmealDishService setmealdishService;
    @Autowired
    CategoryService categoryService;

    /**
     * 新增套餐，用Dto来接受数据,多表操作，对套餐表操作，套餐和菜品关联表操作，REST风格，对象要声明注解
     * @param setmealDto
     * @return
     */
    @PostMapping
    @CachePut(value = "setmeal", key = "#setmealDto.categoryId + '-' + #setmealDto.status")
    public R<String> save(@RequestBody SetmealDto setmealDto){
        setmealService.saveWithDish(setmealDto);
        return R.success("成功");
    }
    /**
     * 套餐修改回显（通过id查询套餐）
     * 需要返回套餐DTO
     */
    @GetMapping("/{id}")
    public R<SetmealDto> show(@PathVariable long id){
        //先把套餐基础的数据查询出来
        Setmeal setmeal = setmealService.getById(id);
        //创建套餐dto,把缺的数据查询补上
        SetmealDto setmealDto = new SetmealDto();
        BeanUtils.copyProperties(setmeal,setmealDto);
        //查询套餐里的菜品
        List<SetmealDish> list = setmealdishService.list(new LambdaQueryWrapper<SetmealDish>().eq(SetmealDish::getId,id));
        //查询套餐分类的类名
        Category category = categoryService.getOne(new LambdaQueryWrapper<Category>().eq(Category::getId, setmealDto.getCategoryId()));
        setmealDto.setCategoryName(category.getName());
        setmealDto.setSetmealDishes(list);
        return R.success(setmealDto);

    }

    /**
     * 套餐分页查询，多表联查，返回DTO的分页
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page,int pageSize,String name){
        Page<Setmeal> pageinfo = new Page(page,pageSize);
        Page<SetmealDto> pageinfodto = new Page(page,pageSize);

        LambdaQueryWrapper<Setmeal> qw=new LambdaQueryWrapper<>();
        qw.like(name!=null,Setmeal::getName,name);
        qw.orderByDesc(Setmeal::getUpdateTime);
        setmealService.page(pageinfo, qw);

        BeanUtils.copyProperties(pageinfo,pageinfodto,"records");
        List<Setmeal> records = pageinfo.getRecords();
        List<SetmealDto> SetmealDtoList = records.stream().map((item) -> {
            SetmealDto setmealDto = new SetmealDto();
            //对象拷贝
            BeanUtils.copyProperties(item, setmealDto);
            //分类id
            Long categoryId = item.getCategoryId();
            //根据分类id查询对象
            Category category = categoryService.getById(categoryId);
            if (category != null) {
                //分类名称
                String categoryName = category.getName();
                setmealDto.setCategoryName(categoryName);
            }
            return setmealDto;
        }).collect(Collectors.toList());
        pageinfodto.setRecords(SetmealDtoList);
        return R.success(pageinfodto);
    }

    /**
     * 删除套餐，多表删除，REST风格，@RequestParam接变量（批量删除多个id）
     * allEntries = true 清除所有
     * @param ids
     * @return
     */
    @DeleteMapping
    @CacheEvict(value = "setmeal",allEntries = true)
    public R<String> delete(@RequestParam List<Long> ids){
        setmealService.removeWithDish(ids);
        return R.success("成功");
    }

    /**
     * 根据条件查询套餐数据，Dto,用springcache做缓存，要把R实现序列化,实现Serializable接口
     * @param setmeal
     * @return
     */
    @Cacheable(value = "setmeal",key = "#setmeal.categoryId + '-' + #setmeal.status" )
    @GetMapping("/list")
    public R<List<Setmeal>> list(Setmeal setmeal){
        LambdaQueryWrapper<Setmeal> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(setmeal.getCategoryId()!=null,Setmeal::getCategoryId,setmeal.getCategoryId());
        queryWrapper.eq(setmeal.getStatus()!=null,Setmeal::getStatus,setmeal.getStatus());
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);
        List<Setmeal> list=setmealService.list(queryWrapper);
        return R.success(list);
    }

    /**
     *  套餐售卖状态
     * @param status
     * @param ids
     * @return
     */
    @PostMapping("/status/{status}")
    public R<String> satatus(@PathVariable(value="status") Integer status,@RequestParam(value="ids") List<Long> ids){
        //把需要改的对象查出来
        List<Setmeal> setmeals = setmealService.listByIds(ids);
        //修改
        for (Setmeal setmeal: setmeals) {
            setmeal.setStatus(status);
            setmealService.updateById(setmeal);
        }
        return R.success("662.6");
    }

    /**
     * 修改套餐
     * @param setmealDto
     * @return
     */
    @PutMapping
    @CacheEvict(value = "setmeal",key = "#setmealDto.categoryId + '-' + #setmealDto.status" )
    public R<String> update(@RequestBody SetmealDto setmealDto){
        setmealService.updateWithDish(setmealDto);
        return R.success("成功");
    }
}
