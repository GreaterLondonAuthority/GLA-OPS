/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class GlaOpsUtils {

    static Logger log = LoggerFactory.getLogger(GlaOpsUtils.class);

    /**
     * @param string a string representation of an integer.
     * @return the parsed integer value or null if the given value wasn't an integer.
     */
    public static Integer parseInt(String string) {
        try {
            return Integer.parseInt(string);
        }
        catch (Exception e) {
            return null;
        }
    }

    public static BigDecimal addBigDecimals(BigDecimal bd1,  BigDecimal bd2) {
        if (bd1 == null && bd2 == null) {
            return null;
        }

        if (bd1 == null) return bd2;
        if (bd2 == null) return bd1;

        return bd1.add(bd2);
    }

    public static BigDecimal addBigDecimals(BigDecimal ... bigDecimals) {
        BigDecimal result = new BigDecimal(0);
        for (BigDecimal bd: bigDecimals) {
            result = addBigDecimals(result, bd);
        }
        return result;
    }

    public static int compareBigDecimals(BigDecimal bd1,  BigDecimal bd2) {
        if (bd1 == null && bd2 == null) {
            return 0;
        }

        if (bd1 == null) return -1;
        if (bd2 == null) return 1;

        return bd1.compareTo(bd2);
    }

    public static boolean areEqual(BigDecimal bd1,  BigDecimal bd2) {
        if (bd1 == null && bd2 == null) {
            return true;
        }

        if (bd1 != null && bd2 != null) {
            return bd1.compareTo(bd2) == 0;
        }

        return false;
    }

    public static Integer nullSafeAdd(Integer ... numbers) {
        Integer sum = 0;
        for (Integer n: numbers) {
            sum += (n == null ? 0 : n);
        }
        return sum;
    }



    public static Long nullSafeAdd(Long ... numbers) {
        Long sum = 0L;
        for (Long n: numbers) {
            sum += (n == null ? 0 : n);
        }
        return sum;
    }

    public static Integer nullSafeMultiply(Integer ... numbers) {
        Integer result = 1;
        for (Integer n: numbers) {
            if (n == null) {
                return null;
            }
            else {
                result *= n;
            }
        }
        return result;
    }

    /**
     * Returns true if both arguments are equal; either both null, or both non-null and matched via equals().
     */
    public static boolean nullSafeEquals(Object o1, Object o2) {
        if ((o1 == null) && (o2 == null)) {
            return true;
        }
        if ((o1 == null) || (o2 == null)) {
            return false;
        }
        return o1.equals(o2);
    }

    public static String getStackTraceAsString(Throwable error) {
        if (error == null) {
            return "";
        }
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        if (error.getMessage() != null) {
            sw.write(error.getMessage());
            sw.write("\n");
        }
        error.printStackTrace(pw);
        return sw.toString();
    }

    /**
     * Returns a string with all the values in the list, separated by commas.
     */
    public static String listToString(List values) {
        if (values == null) {
            return "";
        }
        if (values.size() == 0) {
            return "";
        }
        String result = "";
        for (Object value : values) {
            if (result.length() > 0) {
                result += ",";
            }
            result += value.toString();
        }
        return result;
    }

    public static boolean notNull(final Object o) {
        return o != null;
    }

    public static String getFileContent(File file) {
        try {
            return new String(Files.readAllBytes(file.toPath()));
        } catch (IOException e) {
            log.error("Error reading file " + file.getName(), e);
            return null;
        }
    }

    public static List createNullIgnoringList() {
        return new ArrayList() {
            @Override
            public boolean add(Object o) {
                if (o == null) {
                    return true;
                }
                else {
                    return super.add(o);
                }
            }
        };
    }

    public static boolean isNullOrEmpty(final String externalId) {
        return externalId == null || externalId.isEmpty();
    }

    /**
     * Converts non-breaking spaces to normal spaces.
     */
    public static String breakAllSpaces(String original) {
        return original.toString().replace((char)160,' ');
    }

    /**
     * Enhanced trim() function. Trims non-breaking spaces as well.
     */
    public static String superTrim(String original) {
        if (original == null) {
            return null;
        }
        return breakAllSpaces(original).trim();
    }

    public static List<String> csStringToList(String commaSeparatedString) {
        if (commaSeparatedString == null || commaSeparatedString.isEmpty()) {
            return Collections.emptyList();
        }
        else {
            return Arrays.asList(commaSeparatedString.split(","));
        }
    }

    public static String listToCsString(List<String> list) {
        if (list != null) {
            return StringUtils.join(list, ",");
        }
        else {
            return "";
        }
    }

    public static OffsetDateTime parseDateString(String dateString, String format) {
        if (StringUtils.isNotEmpty(dateString)) {
            if (dateString.length() != format.length()) {
                dateString = dateString.substring(0, format.length());
            }
            return LocalDateTime.parse(dateString, DateTimeFormatter.ofPattern(format)).atZone(ZoneId.systemDefault()).toOffsetDateTime();
        }
        else {
            return null;
        }
    }

}
