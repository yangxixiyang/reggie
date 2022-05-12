package reggie.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 配置mybatisplus分页插件
 */
@Configuration
public class MybatisPlusConfig {
    /**
     * 采用配置类的方式配置分页插件（拦截器），把配置对象存到spring容器里
     * @return
     */
    @Bean
    public MybatisPlusInterceptor MybatisPlusInterceptor(){
        MybatisPlusInterceptor MybatisPlusInterceptor=new MybatisPlusInterceptor();
        //给对象配置一个mybatisplus提供的PaginationInnerInterceptor，就可以使用了
        MybatisPlusInterceptor.addInnerInterceptor(new PaginationInnerInterceptor());
        return MybatisPlusInterceptor;
    }
}
