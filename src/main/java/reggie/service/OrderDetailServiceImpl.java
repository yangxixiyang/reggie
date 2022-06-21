package reggie.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import org.springframework.stereotype.Service;
import reggie.mapper.OrderDetailMapper;
import reggie.pojo.OrderDetail;

/**
* @author shinelon
* @description 针对表【order_detail(订单明细表)】的数据库操作Service实现
* @createDate 2022-06-21 17:25:27
*/
@Service
public class OrderDetailServiceImpl extends ServiceImpl<OrderDetailMapper, OrderDetail> implements OrderDetailService{

}
