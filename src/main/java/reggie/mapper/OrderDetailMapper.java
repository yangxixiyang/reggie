package reggie.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import reggie.pojo.OrderDetail;

/**
* @author shinelon
* @description 针对表【order_detail(订单明细表)】的数据库操作Mapper
* @createDate 2022-06-21 17:25:27
* @Entity .OrderDetail
*/
@Mapper
public interface OrderDetailMapper extends BaseMapper<OrderDetail> {


}
