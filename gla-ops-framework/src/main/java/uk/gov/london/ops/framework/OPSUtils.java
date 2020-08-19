/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.framework;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import uk.gov.london.ops.framework.exception.ValidationException;
import uk.gov.london.ops.framework.security.MockAuthentication;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public class OPSUtils {

    public static String currentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            return authentication.getName();
        } else {
            return null;
        }
    }

    public static void mockCurrentUsername(String username) {
        Authentication authentication = new MockAuthentication(username);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    public static void verifyBinding(String summary, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new ValidationException(summary, bindingResult.getFieldErrors());
        }
    }

    public static BigDecimal toBigDecimal(Long value) {
        return value != null ? BigDecimal.valueOf(value) : BigDecimal.ZERO;
    }

    public static String toMonetaryString(BigDecimal bigDecimal) {
        if (bigDecimal == null) {
            bigDecimal = BigDecimal.ZERO;
        }
        return NumberFormat.getCurrencyInstance(Locale.UK).format(bigDecimal.doubleValue());
    }

}
