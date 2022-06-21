package reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import reggie.pojo.Orders;

public interface OrdersService extends IService<Orders> {
    public void submit(Orders orders);
}
