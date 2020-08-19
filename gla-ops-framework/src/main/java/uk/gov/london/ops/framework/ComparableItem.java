/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.framework;

/**
 * Created by chris on 07/08/2017.
 */
public interface ComparableItem {

    String getComparisonId();

    static boolean areEqual(ComparableItem item, ComparableItem other) {
        if (item == null || other == null) {
            return false;
        }
        return item.getComparisonId().equals(other.getComparisonId());
    }

}
