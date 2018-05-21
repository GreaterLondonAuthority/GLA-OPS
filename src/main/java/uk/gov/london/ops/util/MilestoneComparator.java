/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.util;

import uk.gov.london.ops.domain.project.Milestone;

import java.util.Comparator;

public class MilestoneComparator extends BaseComparator implements Comparator<Milestone> {

    @Override
    public int compare(Milestone m1, Milestone m2) {
        // sort by: milestone date (1) ...
        int milestoneDateComparison = compare(m1.getMilestoneDate(), m2.getMilestoneDate());
        if (milestoneDateComparison != 0) {
            return milestoneDateComparison;
        }

        // ... or if milestone dates are the same by display order (2) (and neither is manually created)
        if (! (Boolean.TRUE.equals(m1.isManuallyCreated()) || Boolean.TRUE.equals(m2.isManuallyCreated()))) {
            int orderComparison = compare(m1.getDisplayOrder(), m2.getDisplayOrder());
            if (orderComparison != 0) {
                return orderComparison;
            }
        }


        // ... or if order is the same by creation time (3)
        return compare(m1.getCreatedOn(), m2.getCreatedOn());
    }

}
