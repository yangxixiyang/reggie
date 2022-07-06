package reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.server.Session;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;
import reggie.Util.SMSUtils;
import reggie.Util.ValidateCodeUtils;
import reggie.pojo.R;
import reggie.pojo.User;
import reggie.service.UserService;

import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {
    @Autowired
    UserService userService;
    @Autowired
    RedisTemplate redisTemplate;
    /**
     * 验证码发送，调用了阿里云的api
     * @param user
     * @param session
     * @return
     */
    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession session){
    //获取手机号
        String phone = user.getPhone();
        if(StringUtils.isNotEmpty(phone)){
            //随机生成4位验证码
            String s = ValidateCodeUtils.generateValidateCode(4).toString();
            //调用阿里云API发送短信(没有申请先不用)
//            SMSUtils.sendMessage("签名","模板",phone,s);
            //先打印在控制台
            log.info("code={}",s);
            //将验证码保存到session
//            session.setAttribute(phone,s);
            //将验证码存到redis，有效期五分钟
            redisTemplate.opsForValue().set(phone,s,5, TimeUnit.MINUTES);
            return R.success("手机验证码短信发送成功");
        }
        return R.error("手机号码为空");
    }

    /**
     * 移动端用户登录，post请求传递过来的参数要用对象实体类来接受，要不就是用dto,要不就是用map
     * @param map
     * @param session
     */
    @PostMapping("/login")
    public R<User> login(@RequestBody Map map,HttpSession session){
        //获取手机号
        String phone = map.get("phone").toString();
        //获取验证码
        String code = map.get("code").toString();
//        //从session中取验证码比较
//        Object codeSession = session.getAttribute(phone);
        //从redis里来获取
        Object codeSession = redisTemplate.opsForValue().get(phone);
        //进行对比
        if(codeSession != null &&codeSession.equals(code)){
            //判断是不是新用户，是就注册
            LambdaQueryWrapper<User> qw = new LambdaQueryWrapper<>();
            qw.eq(User::getPhone,phone);
            User user = userService.getOne(qw);
            if(user == null){
                //注册
                user = new User();
                user.setPhone(phone);
                user.setStatus(1);
                userService.save(user);
            }
            //存到session里给过滤器放行
            session.setAttribute("user",user.getId());
            //登陆成功删除redis里的缓存
            redisTemplate.delete(phone);
        return R.success(user);
        }
    return R.error("错误");
    }


}
