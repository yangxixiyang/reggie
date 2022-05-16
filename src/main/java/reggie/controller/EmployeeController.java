package reggie.controller;

import com.alibaba.druid.sql.visitor.functions.If;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.stereotype.Controller;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;
import reggie.pojo.Employee;
 import reggie.pojo.R;
import reggie.service.EmployeeService;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@RestController
@Slf4j
@RequestMapping("/employee")
public class EmployeeController {
    @Autowired
    private EmployeeService employeeService;

    /**
     * 登陆功能
     * @param request
     * @param employee
     * @return
     */
    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest request , @RequestBody Employee employee){
        //将密码加密处理
        String password=employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        //根据页面提交用户名查询数据库
        LambdaQueryWrapper<Employee> queryWrapper =new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUsername,employee.getUsername());
        Employee one = employeeService.getOne(queryWrapper);
        //判断如果没查到数据
        if(one==null){
            return R.error("登陆失败");
        }
        //判断如果查到了但是是不匹配
        if(!one.getPassword().equals(password)){
            return R.error("登陆失败");
        }
        //查看状态
        if(one.getStatus()==0) {
            return R.error("账号禁用");
        }
        //登陆成功把id存到session里方便调用
        request.getSession().setAttribute("employee",one.getId());
        return R.success(one);
    }

    /**
     * 退出功能
     * 前段接收到R里code=1会判定成功自动跳转
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request){
        //清理session里的id
        request.getSession().removeAttribute("id");
        return R.success("退出成功");
    }

    /**
     * 新增员工方法
     * @param request
     * @param employee
     * @return
     */

    @PostMapping
    public R<String> save(HttpServletRequest request, @RequestBody Employee employee){
        //给员工缺少的信息设置初始值
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));
        employee.setCreateTime(LocalDateTime.now());
        employee.setUpdateTime(LocalDateTime.now());
        //获取当前操作用户的id给员工赋值
        Long employeeid = (Long)request.getSession().getAttribute("employee");
        employee.setCreateUser(employeeid);
        employee.setUpdateUser(employeeid);
        System.out.println(employee);
        boolean save = employeeService.save(employee);

        if(save){
            log.info("进来了");
            return R.success("创建成功");
        }
        else {
            return R.error("创建失败哦");
        }
    }

    /**
     * 分页,不是rest风格，不用声明@PathVariable,如果是JSON格式都要声明
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page( int page,  int pageSize, String name){
//        log.info("page进来了");
        Page pageinfo = new Page(page, pageSize);
        //创建条件构造器
         LambdaQueryWrapper<Employee> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        //条件构造器里判断如果name不为空，再做操作
        lambdaQueryWrapper.like(StringUtils.isNotEmpty(name),Employee::getName,name);
        //添加排序条件
        lambdaQueryWrapper.orderByDesc(Employee::getUpdateTime);
        employeeService.page(pageinfo,lambdaQueryWrapper);
        return R.success(pageinfo);
    }

    /**
     * 员工修改
     * @param request
     * @param employee
     * @return
     */
    @PutMapping
    public R<String> update(HttpServletRequest request,@RequestBody Employee employee){
        //设置修改时间修改人
        employee.setUpdateTime(LocalDateTime.now());
        employee.setUpdateUser((Long) request.getSession().getAttribute("employee"));
        employeeService.updateById(employee);
        return R.success("员工信息修改成功");
    }

    /**
     * 根据id查询员工信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<Employee> getById(@PathVariable String id){
        Employee employee = employeeService.getById(id);
        if (employee!=null) {
            return R.success(employee);
        }
        else {
            return R.error("没有查到对应员工");
        }
    }



}
