package reggie.Util;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import reggie.pojo.R;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 全局异常捕获器
 */
@ResponseBody//要返回json
//声明要捕获有这个注解方法
@ControllerAdvice(annotations = {RestController.class, Controller.class})
public class GlobalExceptionHandler {
    /**
     * 注解声明要捕获的异常
     * @param ex
     * @return
     */
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public R<String> exceptionHandler(SQLIntegrityConstraintViolationException ex){
        //判断捕获的异常中是否包含
        if (ex.getMessage().contains("Duplicate entry")){
            //去掉异常中字符串的空格
            String[] split = ex.getMessage().split(" ");
            //拿到异常里重复的名字
            String msg=split[2]+"已存在";
            return R.error(msg);
        }
        return R.error("未知错误");
    }
}
