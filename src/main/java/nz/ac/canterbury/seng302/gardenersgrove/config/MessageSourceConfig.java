package nz.ac.canterbury.seng302.gardenersgrove.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

/**
 * Configuration class for setting up MessageSource, which is used for
 * resolving text messages for different locales
 */
@Configuration
public class MessageSourceConfig {

    // messageSource bean code adapted from:
    // https://howtodoinjava.com/spring-core/resolving-text-messages-in-spring-resourcebundlemessagesource-example/

    /**
     * MessageSource bean, which is responsible for resolving messages
     * for different locales, from external message files.
     * @return the messageSource bean
     */
    @Bean
    public MessageSource messageSource() {
        // ResourceBundleMessageSource is a common implementation for resolving messages from
        // message property files of different locales within a resource bundle
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        // base name of the message resource bundle
        messageSource.setBasename("classpath:messages");
        // default encoding for message property files
        messageSource.setDefaultEncoding("UTF-8");

        return messageSource;
    }
}
