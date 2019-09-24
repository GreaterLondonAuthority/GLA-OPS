/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.service.project;

import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.london.ops.domain.project.*;
import uk.gov.london.ops.payment.FinanceService;

import java.util.Set;

public abstract class BaseProjectFinanceService extends BaseProjectService implements PostCloneNotificationListener {

    @Autowired
    protected FinanceService financeService;

    abstract ProjectBlockType getBlockType();

    protected void setPopulatedYears(BaseFinanceBlock block) {
        Set<Integer> populatedYears = financeService.getPopulatedYearsForBlock(block.getId());
        block.setPopulatedYears(populatedYears);
    }

    @Override
    public void handleBlockClone(Project project, Integer originalBlockId, Integer newBlockId) {
        NamedProjectBlock projectBlockById = project.getProjectBlockById(originalBlockId);
        // check if correct block type
        if (projectBlockById != null && getBlockType().equals(projectBlockById.getBlockType())) {
            financeService.cloneLedgerEntriesForBlock(originalBlockId, newBlockId);
        }
    }

    @Override
    public void handleProjectClone(Project oldProject, Integer originalBlockId, Project newProject, Integer newBlockId) {
        NamedProjectBlock projectBlockById = oldProject.getProjectBlockById(originalBlockId);
        // check if correct block type
        if (projectBlockById != null && getBlockType().equals(projectBlockById.getBlockType())) {
            financeService.cloneLedgerEntriesForBlock(originalBlockId, newProject.getId(), newBlockId);
        }
    }

}
