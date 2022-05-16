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

}
