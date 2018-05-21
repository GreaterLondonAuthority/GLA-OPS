/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.spe;

/**
 * Created by chris on 23/02/2017.
 */
public class SimpleProjectExportUtils {

    public static String formatForExport(String input) {
        return input.toLowerCase().replaceAll(" ", "_");
    }

}
