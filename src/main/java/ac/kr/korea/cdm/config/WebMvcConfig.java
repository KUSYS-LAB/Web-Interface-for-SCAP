package ac.kr.korea.cdm.config;

import ac.kr.korea.cdm.filter.CorsFilter;
import ac.kr.korea.cdm.filter.TokenFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class    WebMvcConfig implements WebMvcConfigurer {
    private static final Logger logger = LoggerFactory.getLogger(WebMvcConfig.class);


    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        logger.info("WebMvcConfig-addResourceHandlers");
        registry.addResourceHandler("/resources/**").addResourceLocations("classpath:/resources/");
        registry.addResourceHandler("/static/**").addResourceLocations("classpath:/static/");
        registry.addResourceHandler("/assets/**").addResourceLocations("classpath:/static/assets/");
        registry.addResourceHandler("/css/**").addResourceLocations("classpath:/static/css/");
        registry.addResourceHandler("/js/**").addResourceLocations("classpath:/static/js/");
        registry.addResourceHandler("/scss/**").addResourceLocations("classpath:/static/scss/");
        registry.addResourceHandler("/vendor/**").addResourceLocations("classpath:/static/vendor/");
        WebMvcConfigurer.super.addResourceHandlers(registry);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**").allowedOrigins("*");
    }

    //    @Bean
//    public FilterRegistrationBean<TokenFilter> tokenFilter () {
//        FilterRegistrationBean<TokenFilter> registrationBean = new FilterRegistrationBean<>();
//        registrationBean.setFilter(new TokenFilter());
//        registrationBean.addUrlPatterns("/**");
//        return registrationBean;
//    }

    @Bean
    public TokenFilter tokenFilter () {
        return new TokenFilter();
    }

//    @Bean
//    public CorsFilter corsFilter() { return new CorsFilter(); }
}
