package tz.co.neelansoft.Utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by landre on 27/06/2018.
 */

public class EmailValidationUtil {
    private final Pattern pattern;

    private static final String EMAIL_PATTERN =
            "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                    + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

    public EmailValidationUtil() {
        pattern = Pattern.compile(EMAIL_PATTERN);
    }

    /**
     * Validate hex with regular expression
     *
     * @param hex
     *            hex for validation
     * @return true valid hex, false invalid hex
     */
    public boolean isValid(final String hex) {

        Matcher matcher = pattern.matcher(hex);
        return !matcher.matches();

    }
}
