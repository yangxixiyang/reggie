package reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import reggie.pojo.User;
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
