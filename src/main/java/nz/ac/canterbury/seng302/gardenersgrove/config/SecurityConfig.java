package nz.ac.canterbury.seng302.gardenersgrove.config;

import nz.ac.canterbury.seng302.gardenersgrove.component.CustomAuthenticationFailureHandler;
import nz.ac.canterbury.seng302.gardenersgrove.component.CustomAuthenticationForbiddenHandler;
import nz.ac.canterbury.seng302.gardenersgrove.component.CustomAuthenticationProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.DelegatingSecurityContextRepository;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 * Custom Security Configuration class for defining security settings with Spring Security
 * Such functionality was previously handled by WebSecurityConfigurerAdapter
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    /**
     Auto-wires a CustomAuthenticationProvider for authentication logic.
     This provider implements Spring Security's AuthenticationProvider interface.
     The required = false ensures bean creation doesn't fail when security is disabled.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Autowired
    private CustomAuthenticationProvider authProvider;

    // Configure the global authentication manager used by Spring Security
    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) {
        auth.authenticationProvider(authProvider);
    }


    /*
     Configures and provides the AuthenticationManager, using the
     AuthenticationManagerBuilder from HttpSecurity, and sets up
     a custom authentication provider.
     */
    @Bean
    public AuthenticationManager authManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder.authenticationProvider(authProvider);
        return authenticationManagerBuilder.build();
    }

    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new DelegatingSecurityContextRepository(
                new RequestAttributeSecurityContextRepository(),
                new HttpSessionSecurityContextRepository()
        );
    }

    // Configure the SecurityFilterChain based on whether security is enabled or not.
    //
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, SecurityContextRepository securityContextRepository) throws Exception {
        // If security is enabled, configure authentication and authorization rules.
                                /*
                    specify what requirements are needed for users to access certain URLs
                    e.g.
                    - permitAll() - allowed without any authentication
                    - authenticated() - user must be authenticated
                    - hasRole("ROLE_NAME") - user must have a certain role
                    - hasAnyRole("ROLE1", "ROLE2", ...) - user must have one of these roles
                    - hasAuthority("AUTHORITY_NAME") - user must have a certain role
                    - hasAnyAuthority("AUTH1", "AUTH2", ...) - user must have one of these authorities
                     */
            http.authorizeHttpRequests(auth -> auth
                            .requestMatchers("/css/**").permitAll()
                            .requestMatchers("/js/**").permitAll()
                            .requestMatchers("/reset").permitAll()
                            .requestMatchers("/forgot").permitAll()
                            .requestMatchers("/homepage").permitAll()
                            .requestMatchers("/").permitAll()
                            .requestMatchers("/register").permitAll()
                            .requestMatchers("/h2/**").permitAll()
                            .requestMatchers("/user_uploads/**").permitAll()
                            .requestMatchers("/login").permitAll()
                            .requestMatchers("/activate").permitAll()
                            .anyRequest().hasAuthority("ROLE_USER_VERIFIED")) // anyRequest: anything else not specified
                    // Disable header settings for improved security
                    .headers(AbstractHttpConfigurer::disable)
                    // Disable CSRF protection for H2 Console requests as it's not required
                    .csrf(csrf -> csrf
                            .ignoringRequestMatchers(new AntPathRequestMatcher("/h2/**")))
                    // Configure form-based login with a custom login page and default success URL
                    .formLogin(form -> form
                            .loginPage("/login")
                            // Spring security uses the "username" and "password"
                            // parameters for logging in.
                            // Since our login uses emails instead of username, we can
                            // specify to use the "email" field in place of the username
                            .usernameParameter("email")
                            .defaultSuccessUrl("/homepage", true)
                            // Sends failed login requests to the customAuthenticationFailureHandler
                            .failureHandler(customAuthenticationFailureHandler()))
                            .exceptionHandling((exceptions) -> exceptions
                            .accessDeniedHandler(customAuthenticationForbiddenHandler())
                    )
                    // Configure logout functionality with logout URL, success URL,
                    // invalidating HTTP session, and deleting session cookie
                    .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "GET")) // Allow GET for logout important to remove when released.
                        .logoutSuccessUrl("/homepage")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID"))
                    .securityContext(securityContext -> securityContext
                            .securityContextRepository(securityContextRepository));

        // Build and return the configured SecurityFilterChain
        return http.build();
    }

    @Bean
    public CustomAuthenticationFailureHandler customAuthenticationFailureHandler() {
        return new CustomAuthenticationFailureHandler();
    }

    @Bean
    public CustomAuthenticationForbiddenHandler customAuthenticationForbiddenHandler() {
        return new CustomAuthenticationForbiddenHandler();
    }
}
