/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.util;

import javax.servlet.http.HttpServletResponse;

/**
 * Created by dieppa on 22/03/17.
 */
public class ExporterUtils {



    public static void csvResponse(final HttpServletResponse response,
                                   final String fileName) {
        response.addHeader("Content-disposition", "attachment;filename=" + fileName);
        response.setContentType("text/csv");
    }

}
