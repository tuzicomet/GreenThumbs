package nz.ac.canterbury.seng302.gardenersgrove.component;

import nz.ac.canterbury.seng302.gardenersgrove.entity.AbstractUser;
import nz.ac.canterbury.seng302.gardenersgrove.exceptions.AccountDisabledException;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {

    @Autowired
    private UserService userService;

    private ApplicationContext applicationContext;

    @Autowired
    public CustomAuthenticationProvider(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * Handles authentication through spring security, uses the password encoder to ensure
     * the match is for the encrypted password and not the plain text.
     *
     * @param authentication passes and authentication type for authorities
     */
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String email = authentication.getName();
        String password = authentication.getCredentials().toString();

        AbstractUser user = userService.getUserByEmail(email);
        if (user != null) {
            PasswordEncoder passwordEncoder = applicationContext.getBean(PasswordEncoder.class);
            // If the account is banned, throw AccountDisabledException with the number of days they're banned for
            if (user.getAccountDisabledUntil().isAfter(Instant.now())) {
                throw new AccountDisabledException(
                        Long.toString(Instant.now().until(user.getAccountDisabledUntil(), ChronoUnit.DAYS))
                );
            }
            // If the login details are correct then return a valid authentication token.
            if (passwordEncoder.matches(password, user.getPassword())) {
                return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
            }
        }
        // If the credentials were wrong send and exception to be shown on the front
        throw new BadCredentialsException("Invalid username or password");
    }

    /**
     * Confirms if the authentication is valid for the correct token.
     *
     * @param authentication    email of the user
     * @return boolean indicating if the authentication is correct.
     */
    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }

}
