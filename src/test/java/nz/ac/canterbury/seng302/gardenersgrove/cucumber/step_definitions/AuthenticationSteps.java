package nz.ac.canterbury.seng302.gardenersgrove.cucumber.step_definitions;

import io.cucumber.java.en.Given;
import nz.ac.canterbury.seng302.gardenersgrove.component.CustomAuthenticationProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class AuthenticationSteps {
    @Autowired
    private CustomAuthenticationProvider customAuthProvider;

    @Given("I am logged in with email {string} and password {string}")
    public void iAmLoggedInWithEmailAndPassword(String email, String password) {
        Authentication authentication = customAuthProvider.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

}
