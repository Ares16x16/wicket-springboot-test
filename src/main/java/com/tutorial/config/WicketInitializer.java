package com.tutorial.config;

import org.apache.wicket.protocol.http.WicketFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WicketInitializer {
    @Bean
    public FilterRegistrationBean<WicketFilter> wicketServletRegistration() {
        FilterRegistrationBean<WicketFilter> registration = new FilterRegistrationBean<>(new WicketFilter());
        registration.addUrlPatterns("/*");
        registration.addInitParameter(WicketFilter.APP_FACT_PARAM, "com.tutorial.config.WicketApplication");
        registration.setName("WicketFilter");
        return registration;
    }
}
