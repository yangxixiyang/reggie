package reggie.filter;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;
import reggie.pojo.R;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
@Slf4j
@WebFilter(filterName = "loginCheckFilter",urlPatterns = "/*")
public class LoginCheckFilter implements Filter {
    //创建路径匹配器对象，支持通配符，用它与我们定义的string数组路径进行匹配
    public static final AntPathMatcher PATH_MATCHER=new AntPathMatcher();
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request=(HttpServletRequest) servletRequest;
        HttpServletResponse response=(HttpServletResponse) servletResponse;
        //获得请求路径
        String requestURI = request.getRequestURI();
        //定义放行的请求路径数组(静态资源页面都放行，可以拦截ajax请求就行了)
        String[] urls=new String[]{"/employee/login",
                "/employee/logout",
                "/backend/**",
                "/front/**"};
        //创建函数调用与路径进行匹配
        boolean check = check(urls, requestURI);
        log.info("拦截到请求{}",requestURI);
        //如果不需要处理，直接放行
        if(check){
            filterChain.doFilter(request,response);
            log.info("放行了");
            return;
        }
        //判断登陆状态，如果已经登陆，直接放行,登陆成功会把id存到session里
        if(request.getSession().getAttribute("employee")!=null){
            filterChain.doFilter(request,response);
            return;
        }
        //如果未登录返回未登录结果给前端，前端设置好跳转，通过输出流的方式响应（因为这是过滤器不是controller）
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
        return;


    }
    public boolean check(String[] urls,String requestURI){
        for(String url:urls){
            if(PATH_MATCHER.match(url,requestURI)){
                return true;
            }
        }
        return false;
    }
}
