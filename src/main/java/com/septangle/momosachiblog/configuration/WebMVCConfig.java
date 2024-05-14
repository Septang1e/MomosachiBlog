package com.septangle.momosachiblog.configuration;


import com.septangle.momosachiblog.filter.AuthHandlerInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import java.util.List;

@Slf4j
@Configuration
public class WebMVCConfig extends WebMvcConfigurationSupport {

    @Autowired
    private AuthHandlerInterceptor authHandlerInterceptor;

    /**
     * 配置跨域
     * @param registry
     */
    /**
     * 在使用springSecurity之前的解决方案
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        //跨域配置：前端是8080端口，后端是8888
        //要允许8080访问接口服务

        log.info("正在配置跨域请求");

        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowCredentials(true)
                .allowedMethods("GET","POST","DELETE")
                .allowedOriginPatterns("*")
                .maxAge(3600)
        ;
    }
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();

        List<String> allowedOrigins = new ArrayList<>();
        allowedOrigins.add("http://localhost:5173");

        corsConfiguration.setAllowedOrigins(allowedOrigins);

        //注册mappers
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);
        return source;
    }
     **/


    @Override
    protected void extendMessageConverters(List<HttpMessageConverter<?>> converters)
    {
        log.info("扩展消息转换器...");
        //创建消息转换器对象
        MappingJackson2HttpMessageConverter messageConverter=new MappingJackson2HttpMessageConverter();
        //设置对象转换器，底层使用Jackson将Java对象转换成json
        messageConverter.setObjectMapper(new JacksonObjectMapper());
        //index表示优先级，0为最优先
        converters.add(0, messageConverter);
    }
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry
                .addInterceptor(authHandlerInterceptor);
    }
}

