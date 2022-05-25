package reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reggie.pojo.Category;
import reggie.pojo.R;
import reggie.service.CategoryService;

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

}
