package reggie.service;

//import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import reggie.mapper.UserMapper;
import reggie.pojo.User;
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper,User> implements UserService{
}
