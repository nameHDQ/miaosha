package com.hdq.miaosha.config;

import com.hdq.miaosha.domain.MiaoshaUser;
import com.hdq.miaosha.service.MiaoshaUserServicce;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * 自定义参数解析器
 * 补充：能使用@autowired注解bean的前提是 本身是容器里的一个bean
 * 即也得注解@Service
 * @author hdq
 */
@Service
public class UserArgumentResolver implements HandlerMethodArgumentResolver {


	@Autowired
	private MiaoshaUserServicce userService;

	/**
	 * 判断是否支持该参数解析器，先判断参数的类型是不是要用的  如果是就返回true然后执行resolveArgument方法
	 * 如果不是就返回false 不执行resolveArgument方法
	 * @param parameter 参数
	 * @return
	 */
	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		Class<?> clazz = parameter.getParameterType();
		return clazz== MiaoshaUser.class;
	}

	/**
	 * 解析参数
	 * @param parameter
	 * @param mavContainer
	 * @param webRequest
	 * @param binderFactory
	 * @return
	 * @throws Exception
	 */
	@Override
	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
								  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
		HttpServletRequest nativeRequest = webRequest.getNativeRequest(HttpServletRequest.class);
		HttpServletResponse nativeResponse = webRequest.getNativeResponse(HttpServletResponse.class);
		// 一般为了兼容游览器 会把token放在游览器的请求参数里面 所以也要判断请求参数里面是否有token
		// 游览器的请求参数里面的token优先级高于cookie里面的token
		String paraToken = nativeRequest.getParameter(MiaoshaUserServicce.COOKIE_NAME_TOKEN);
		String cookieToken = getCookieValue(nativeRequest, MiaoshaUserServicce.COOKIE_NAME_TOKEN);
		if (StringUtils.isEmpty(paraToken) && StringUtils.isEmpty(cookieToken)) {
			return null;
		}
		String token = StringUtils.isEmpty(paraToken) ? cookieToken : paraToken;
		return userService.getByToken(nativeResponse, token);
	}

	/**
	 * 获取cookie的值
	 * @param request
	 * @param cookieName
	 * @return
	 */
	// TODO:异常处理没有做
	private String getCookieValue(HttpServletRequest request, String cookieName) {
		// 获取所有的cookie
		Cookie[] cookies = request.getCookies();
		for (Cookie cookie : cookies) {
			if (cookie.getName().equals(cookieName)) {
				return cookie.getValue();
			}
		}
		return null;
	}

}
