package com.hdq.miaosha.config;

import com.hdq.miaosha.access.AccessInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;


/**
 * 在Springboot中的WebMvcConfigurer接口在Web开发中经常被使用，例如配置拦截器、配置ViewController、配置Cors跨域等
 * @author hdq
 */
@Configuration
public class WebConfig  implements WebMvcConfigurer{

	/**
	 * 自定义的参数处理器
	 */
	@Autowired
	UserArgumentResolver userArgumentResolver;
	
	@Autowired
	AccessInterceptor accessInterceptor;

	/**
	 * 添加自定义的参数处理器
	 * @param argumentResolvers 参数处理器列表
	 */
	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
		argumentResolvers.add(userArgumentResolver);
	}
	
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(accessInterceptor);
	}
	
}
