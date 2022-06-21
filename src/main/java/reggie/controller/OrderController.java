package reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reggie.Util.BaseContext;
import reggie.pojo.Orders;
import reggie.pojo.R;
import reggie.service.OrdersService;

import java.util.List;

@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrdersService ordersService;

    /**
     * 订单提交
     * @param orders
     * @return
     */
    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders){
        ordersService.submit(orders);
        return R.success("成");
    }

    /**
     * 订单分页查询
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/userPage")
    public R<Page> list(int page,int pageSize){
        Page pages=new Page<Orders>(page,pageSize);
        //获取用户id
        long currentId = BaseContext.getCurrentId();
        LambdaQueryWrapper<Orders> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Orders::getUserId,currentId);
        lambdaQueryWrapper.orderByAsc(Orders::getOrderTime);

        Page page1 = ordersService.page(pages, lambdaQueryWrapper);
        return R.success(pages);
    }



}
