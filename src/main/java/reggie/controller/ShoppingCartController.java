package reggie.controller;

import com.alibaba.druid.sql.visitor.functions.If;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reggie.Util.BaseContext;
import reggie.pojo.R;
import reggie.pojo.ShoppingCart;
import reggie.service.ShoppingCartService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("shoppingCart")
public class ShoppingCartController {
    @Autowired
    ShoppingCartService shoppingCartService;

    /**
     * 添加购物车功能
     *
     * @param shoppingCart
     * @return
     */
    @PostMapping("/add")
    public R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart) {
        //设置用户id，指定是哪个用户的购物车数据，用户id在登陆时就被存到域里了
        long currentId = BaseContext.getCurrentId();
        shoppingCart.setUserId(currentId);
        //查询当前添加的菜品或套餐在不在购物车里，如果在的话只需要数量+1就可以了
        Long dishId = shoppingCart.getDishId();
        //用户id为查询购物车条件
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, currentId);
        if (dishId != null) {
            //证明是菜品
            queryWrapper.eq(ShoppingCart::getDishId, dishId);
        } else {
            //证明是套餐
            queryWrapper.eq(ShoppingCart::getSetmealId, shoppingCart.getSetmealId());
        }
        //先查询该菜品或套餐在不在购物车里
        //SQL:select * from shoppingCart where user_id = ?(currentid) and dishid=?(dishid) or mealid=?(mealid)
        ShoppingCart cartServiceOne = shoppingCartService.getOne(queryWrapper);
        if (cartServiceOne != null) {
            //如果已经存在，就在数量上加1
            Integer number = cartServiceOne.getNumber();
            cartServiceOne.setNumber(number + 1);
            shoppingCartService.updateById(cartServiceOne);
        } else {
            //如果不存在，添加到数据库,默认数量是1
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartService.save(shoppingCart);
            cartServiceOne = shoppingCart;
        }
        return R.success(cartServiceOne);
    }

    /**
     * 显示购物车，查看购物车
     *
     * @return
     */
    @GetMapping("/list")
    public R<List<ShoppingCart>> list() {
        //用户id查
        LambdaQueryWrapper<ShoppingCart> shoppingCartLambdaQueryWrapper = new LambdaQueryWrapper<>();
        shoppingCartLambdaQueryWrapper.eq(ShoppingCart::getUserId, BaseContext.getCurrentId());
        //通过创建时间来排序
        shoppingCartLambdaQueryWrapper.orderByAsc(ShoppingCart::getCreateTime);
        List<ShoppingCart> list = shoppingCartService.list(shoppingCartLambdaQueryWrapper);
        return R.success(list);
    }

    /**
     * 删除菜品功能
     * @param shoppingCart
     * @return
     */
    @PostMapping("/sub")
    public R<String> sub(@RequestBody ShoppingCart shoppingCart) {
        //设置用户id，指定是哪个用户的购物车数据，用户id在登陆时就被存到域里了
        long currentId = BaseContext.getCurrentId();
        shoppingCart.setUserId(currentId);
        //查询当前的菜品或套餐在不在购物车里，如果在的话只需要数量-1就可以了
        Long dishId = shoppingCart.getDishId();
        //用户id为查询购物车条件
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, currentId);
        if (dishId != null) {
            //证明是菜品
            queryWrapper.eq(ShoppingCart::getDishId, dishId);
        } else {
            //证明是套餐
            queryWrapper.eq(ShoppingCart::getSetmealId, shoppingCart.getSetmealId());
        }
        //先查询该菜品或套餐在不在购物车里
        //SQL:select * from shoppingCart where user_id = ?(currentid) and dishid=?(dishid) or mealid=?(mealid)
        ShoppingCart cartServiceOne = shoppingCartService.getOne(queryWrapper);
        if (cartServiceOne != null) {
            //在购物车里如果只有一个数量直接删除，数量大于2就先减1
            if (cartServiceOne.getNumber()<2){
                shoppingCartService.remove(queryWrapper);
            }else {
                Integer number = cartServiceOne.getNumber();
                cartServiceOne.setNumber(number - 1);
                shoppingCartService.updateById(cartServiceOne);
            }

        }
        return R.success("成功");
    }

    /**
     * 清空购物车功能
     * @return
     */
    @DeleteMapping("/clean")
    public R<String> clean(){
        //获取用户id
        long currentId = BaseContext.getCurrentId();
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,currentId);
        shoppingCartService.remove(queryWrapper);
        return R.success("成功");
    }
}