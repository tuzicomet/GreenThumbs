package nz.ac.canterbury.seng302.gardenersgrove.utility;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import java.util.Locale;

/**
 * Utility class containing methods to handle locale on the server side
 */
public class LocaleUtils {

    /**
     * Returns the current locale from the passed in HttpServletRequest.
     * @param request the HttpServletRequest which contains the session
     * @return the current locale, based on the value stored in the session. Defaults to English locale if not found.
     */
    public static Locale getLocaleFromSession(HttpServletRequest request) {
        // get the session from the HttpServletRequest
        HttpSession session = request.getSession(false);
        if (session != null) {
            // The Locale, by default, is stored in the LOCALE_SESSION_ATTRIBUTE_NAME session attribute.
            // Get the locale from here as a Locale type variable
            Locale locale = (Locale) session.getAttribute(SessionLocaleResolver.LOCALE_SESSION_ATTRIBUTE_NAME);
            if (locale != null) {
                return locale;
            }
        }
        // if no locale is found, then return English locale by default
        return Locale.ENGLISH;
    }
}