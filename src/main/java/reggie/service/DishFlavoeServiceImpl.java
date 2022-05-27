package reggie.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import reggie.mapper.DishFlavoeMapper;
import reggie.pojo.DishFlavor;
@Service
public class DishFlavoeServiceImpl extends ServiceImpl<DishFlavoeMapper, DishFlavor> implements DishFlavoeService {
}
