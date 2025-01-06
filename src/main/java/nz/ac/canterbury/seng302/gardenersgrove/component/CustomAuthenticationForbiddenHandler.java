package nz.ac.canterbury.seng302.gardenersgrove.component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;

@Configuration
public class CustomAuthenticationForbiddenHandler implements AccessDeniedHandler {
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                                AccessDeniedException exception) throws IOException, ServletException {
        // Catches the specific type of error and adds attributes based on it
        if (exception instanceof AccessDeniedException) {
            request.getSession().setAttribute("error", "forbidden");
        }
        response.sendRedirect("/");
    }
}