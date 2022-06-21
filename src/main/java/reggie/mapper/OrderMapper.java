package reggie.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import reggie.pojo.Orders;


/**
* @author shinelon
* @description 针对表【orders(订单表)】的数据库操作Mapper
* @createDate 2022-06-21 17:19:30
* @Entity .Orders
*/
@Mapper
public interface OrderMapper extends BaseMapper<Orders> {


}
