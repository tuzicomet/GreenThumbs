package nz.ac.canterbury.seng302.gardenersgrove.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import java.util.Locale;

@Configuration
@EnableWebMvc
public class MvcConfig implements WebMvcConfigurer {

    /**
     * Adds specific paths to resources to get through spring security.
     *
     * @param registry the registry used to add the resources.
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/css/**").addResourceLocations("classpath:/static/css/");
        registry.addResourceHandler("/js/**").addResourceLocations("classpath:/static/js/");
        registry.addResourceHandler("/styles/**").addResourceLocations("/styles/");
        registry.addResourceHandler("/images/**").addResourceLocations("classpath:/static/images/");
        registry.addResourceHandler("/resources/**").addResourceLocations("/resources/");
    }

    /**
     * Bean for resolving and managing the application's locale settings.
     * It determines the current locale to use for language based on the 'lang'
     * parameter present in the request, and updates the application to use this
     * locale, and stores the locale in the browser's session storage.
     */
    @Bean
    public LocaleResolver localeResolver() {
        // Code originally based on this guide:
        // https://www.baeldung.com/spring-boot-internationalization

        // SessionLocaleResolver is a session-based LocaleResolver implementation.
        SessionLocaleResolver slr = new SessionLocaleResolver();
        // By default, this sets the locale to english. Alternatively this can be
        // done by adding 'spring.web.locale=en' to application.properties.
        slr.setDefaultLocale(Locale.of("en"));
        return slr;
    }

    /**
     * An interceptor bean that intercepts HTTP requests, and uses LocaleResolver to switch
     * the current locale to the value of the "lang" parameter when present on any request.
     * Also works using the messageSource bean to dynamically set and update locale.
     */
    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        // This code was sourced from: https://www.baeldung.com/spring-boot-internationalization
        // along with localeResolver, which is located in SecurityConfig
        LocaleChangeInterceptor lci = new LocaleChangeInterceptor();
        lci.setParamName("lang");
        return lci;
    }

    /**
     * An override of the WebMvcConfigurer interface's addInterceptors method, which
     * adds the LocaleChangeInterceptor bean to the application’s interceptor registry.
     * This is required in order for LocaleChangeInterceptor to take effect
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
    }
}
