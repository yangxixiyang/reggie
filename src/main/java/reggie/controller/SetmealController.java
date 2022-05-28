package reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reggie.dto.SetmealDto;
import reggie.pojo.Category;
import reggie.pojo.R;
import reggie.pojo.Setmeal;
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
    public R<String> save(@RequestBody SetmealDto setmealDto){
        setmealService.saveWithDish(setmealDto);
        return R.success("成功");
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
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids){
        setmealService.removeWithDish(ids);
        return R.success("成功");
    }
}
