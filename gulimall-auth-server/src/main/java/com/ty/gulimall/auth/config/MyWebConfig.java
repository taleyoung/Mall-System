package com.ty.gulimall.auth.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
public class MyWebConfig implements WebMvcConfigurer {

	/**
	 * 视图映射
	 * @param registry
	 */

	@Override
	public void addViewControllers(ViewControllerRegistry registry) {

		registry.addViewController("/reg.html").setViewName("reg");
	}
}
