package com.leyou.upload.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class LeyouCorsFilterConfiguration {

    @Bean
    public CorsFilter corsFilter(){
        //初始化配置对象
        CorsConfiguration corsConfiguration = new CorsConfiguration();
       //允许跨越的域名  可以配置多个  *代表所有域名，如果要携带cookie 一定不能设置为*
        corsConfiguration.addAllowedOrigin("http://manage.leyou.com");
        //允许携带cookie
        corsConfiguration.setAllowCredentials(true);
        //允许所有请求方式跨域访问
        corsConfiguration.addAllowedMethod("*");//post get
        //允许携带所有头信息跨域访问
        corsConfiguration.addAllowedHeader("*");


        //初始化配置源对象
        UrlBasedCorsConfigurationSource configuration = new UrlBasedCorsConfigurationSource();
        //拦截所有请求，校验是否允许跨域
        configuration.registerCorsConfiguration("/**",corsConfiguration);

        return  new CorsFilter(configuration) ;
    }
}
