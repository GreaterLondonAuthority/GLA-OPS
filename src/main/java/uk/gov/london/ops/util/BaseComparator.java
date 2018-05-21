/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.util;

public class BaseComparator {

    protected int compare(Comparable c1, Comparable c2) {
        if (c1 != null && c2 != null) {
            return c1.compareTo(c2);
        }
        else if (c1 != null) {
            return -1;
        }
        else if (c2 != null) {
            return 1;
        }
        else { // both null
            return 0;
        }
    }

}
