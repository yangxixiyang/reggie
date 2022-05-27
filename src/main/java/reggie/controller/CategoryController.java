package reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reggie.pojo.Category;
import reggie.pojo.R;
import reggie.service.CategoryService;

import java.util.List;

@RestController
@RequestMapping("/category")
@Slf4j
public class CategoryController {
    @Autowired
    private CategoryService CategoryService;

    /**
     * 保存分类
     * @param category
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody Category category){
        boolean save = CategoryService.save(category);
        if(save!=false){
            return R.success("保存分类成功");
        }
        else {
            return R.error("保存分类失败");
        }
    }

    /**
     * 分类分页显示
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page,int pageSize){
        Page<Category> pageinfo = new Page(page,pageSize);
        //根据sort进行分类排序
        LambdaQueryWrapper<Category> qw=new LambdaQueryWrapper();
        qw.orderByAsc(Category::getSort);
        //查
        CategoryService.page(pageinfo, qw);
        //反
        return R.success(pageinfo);
    }

    /**
     * 删除分类，先判断该分类有没有关联菜品和套餐，前段传过来的数据是ids,接收参数也要写ids，不然报错
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> deletebyid(long ids){
        //手动写方法判断该分类是否关联了菜品和套餐，在CategoryService
        CategoryService.remove(ids);
        return R.success("删除成功");
    }

    /**
     * 分类修改
     * @param category
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody Category category){
        CategoryService.updateById(category);
        return R.success("修改成功");
    }

    /**
     *添加时菜品分类查询
     * @param category
     * @return
     */
    @GetMapping("/list")
    public R<List<Category>> list(Category category){
        //创建条件构造器
        LambdaQueryWrapper<Category> qw = new LambdaQueryWrapper();
        //添加条件
        qw.eq(category.getType()!=null,Category::getType,category.getType());
        //添加排序条件
        qw.orderByAsc(Category::getSort).orderByDesc(Category::getUpdateTime);
        List<Category> list = CategoryService.list(qw);
        return R.success(list);
    }


}
