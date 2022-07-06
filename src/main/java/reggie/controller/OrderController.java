package reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import reggie.Util.BaseContext;
import reggie.pojo.OrderDetail;
import reggie.pojo.Orders;
import reggie.pojo.OrdersDto;
import reggie.pojo.R;
import reggie.service.OrderDetailService;
import reggie.service.OrdersService;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrdersService ordersService;
    @Autowired
    private OrderDetailService orderDetailService;

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
     * 订单分页查询,用户端
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

    /**
     *  订单分页查询管理端,不止是分页，订单号查询和起始结束时间查询也会用到本功能
     *  所以接受参数列表里不用@RequestParam这是绑定了参数映射，确保参数一定会有才使用
     *
     */
    @GetMapping("/page")
    public R<Page> order(int page, int pageSize, Long number, @DateTimeFormat String beginTime, @DateTimeFormat String endTime){
        // 分页构造器
        Page<Orders> pageInfo = new Page<>(page,pageSize); //创建要分页的数据
        Page<OrdersDto> dtoPage = new Page<>(); //要返回给前端的分页数据对象
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Orders::getOrderTime);
        queryWrapper.eq(number!=null,Orders::getId,number); // 根据订单号查询
        queryWrapper.between(beginTime!=null&&endTime!=null,Orders::getCheckoutTime,beginTime,endTime); //根据日期查询
        ordersService.page(pageInfo,queryWrapper); //根据用户id 查询订单表数据
        BeanUtils.copyProperties(pageInfo,dtoPage,"records"); // 拷贝分页信息 不包括records records里面有我们要传给前端的数据
        List<Orders> records = pageInfo.getRecords(); //获取records
        List<OrdersDto>   list   = records.stream().map((item)->{ //
            OrdersDto ordersDto = new OrdersDto(); //创建数据传输对象 数据传输对象继承了 orders 里面还有订单详细字段
            BeanUtils.copyProperties(item,ordersDto); // 把records中的数据拷贝 到数据传输对象中
            Long id = item.getId();             // 获取订单id
            LambdaQueryWrapper<OrderDetail> orderDetailLambdaQueryWrapper = new LambdaQueryWrapper<>(); //创建条件构造器
            orderDetailLambdaQueryWrapper.eq(OrderDetail::getOrderId,id); //根据订单id 去订单详细表中查询 订单详细数据
            List<OrderDetail> orderDetails= orderDetailService.list(orderDetailLambdaQueryWrapper);
            ordersDto.setOrderDetails(orderDetails); //把查询到详细订单信息 添加到数据传输对象中
            return ordersDto;   // 返回数据传输对象
        }).collect(Collectors.toList()); // 把遍历到结果转换为集合
        dtoPage.setRecords(list); //重新添加records
        return R.success(dtoPage);
    }



}
