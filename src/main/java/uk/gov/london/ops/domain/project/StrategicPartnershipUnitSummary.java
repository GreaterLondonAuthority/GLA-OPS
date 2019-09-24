/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.domain.project;

import java.util.Set;

public class StrategicPartnershipUnitSummary {

    Set<AssociatedProjectRequestedAndSOSRecord> associatedRecords;

    Integer approvedAtSoSTotal = 0;
    Integer requestedAtSoSTotal = 0;
    Integer requestedUnitsTotal = 0;
    Integer approvedUnitsTotal = 0;
    Integer unitsPlannedTotal = 0;

    public StrategicPartnershipUnitSummary(Set<AssociatedProjectRequestedAndSOSRecord> associatedRecords) {
        this.associatedRecords = associatedRecords;

        if (associatedRecords != null && !associatedRecords.isEmpty()) {
            for (AssociatedProjectRequestedAndSOSRecord associatedRecord : associatedRecords) {
                approvedAtSoSTotal += associatedRecord.getApprovedAtSoS() == null ? 0 : associatedRecord.getApprovedAtSoS();
                requestedAtSoSTotal += associatedRecord.getRequestedAtSoS() == null ? 0 : associatedRecord.getRequestedAtSoS();
                requestedUnitsTotal += associatedRecord.getRequestedUnits() == null ? 0 : associatedRecord.getRequestedUnits();
                approvedUnitsTotal += associatedRecord.getApprovedUnits() == null ? 0 : associatedRecord.getApprovedUnits();
                unitsPlannedTotal += associatedRecord.getUnitsPlanned() == null ? 0 : associatedRecord.getUnitsPlanned();

            }

        }

    }

    public Set<AssociatedProjectRequestedAndSOSRecord> getAssociatedRecords() {
        return associatedRecords;
    }

    public Integer getApprovedAtSoSTotal() {
        return approvedAtSoSTotal;
    }

    public Integer getRequestedAtSoSTotal() {
        return requestedAtSoSTotal;
    }

    public Integer getRequestedUnitsTotal() {
        return requestedUnitsTotal;
    }

    public Integer getApprovedUnitsTotal() {
        return approvedUnitsTotal;
    }

    public Integer getUnitsPlannedTotal() {
        return unitsPlannedTotal;
    }

}
