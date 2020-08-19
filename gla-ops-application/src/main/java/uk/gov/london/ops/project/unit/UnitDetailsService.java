/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.unit;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.london.ops.audit.AuditService;
import uk.gov.london.ops.framework.exception.ValidationException;
import uk.gov.london.ops.project.BaseProjectService;
import uk.gov.london.ops.project.Project;
import uk.gov.london.ops.project.ProjectService;
import uk.gov.london.ops.project.block.ProjectBlockType;
import uk.gov.london.ops.project.template.domain.TemplateTenureType;
import uk.gov.london.ops.refdata.CategoryValue;
import uk.gov.london.ops.refdata.RefDataService;
import uk.gov.london.ops.user.domain.User;

@Service
@Transactional
public class UnitDetailsService extends BaseProjectService {

    @Autowired
    ProjectService projectService;

    @Autowired
    AuditService auditService;

    @Autowired
    RefDataService refDataService;

    /**
     * Delete an existing table row entry
     *
     * @return true if deleted
     */
    public boolean deleteTableEntry(Integer projectId, Integer blockId, Integer tableRowId) {
        Project project = projectService.get(projectId);
        UnitDetailsBlock block = (UnitDetailsBlock) project.getSingleBlockByTypeAndId(ProjectBlockType.UnitDetails, blockId);
        checkForLock(block);
        if (block.getTableEntries().removeIf(next -> tableRowId.equals(next.getId()))) {
            auditService.auditCurrentUserActivity(String.format(
                    "Removed unit table entry  with ID %d on block with ID %d in Project with ID %d",
                    tableRowId,
                    blockId,
                    projectId));
            projectService.updateProject(project);
            return true;
        } else {
            return false;
        }

    }

    /**
     * create a new table entry
     *
     * @return the updated entry
     */
    public UnitDetailsTableEntry createUnitsEntry(Integer projectId, Integer blockId, UnitDetailsTableEntry unitTableEntry) {
        Project project = projectService.get(projectId);
        UnitDetailsBlock block = (UnitDetailsBlock) project.getSingleBlockByTypeAndId(ProjectBlockType.UnitDetails, blockId);
        checkForLock(block);
        unitTableEntry.setProjectId(projectId);
        validateEntry(unitTableEntry);

        User currentUser = userService.currentUser();

        unitTableEntry.setCreatedOn(environment.now());
        unitTableEntry.setCreatedBy(currentUser.getUsername());

        unitTableEntry.setProjectId(projectId);
        unitTableEntry.setBlockId(blockId);

        block.getTableEntries().add(unitTableEntry);

        Project projectUpdated = this.updateProject(project);

        block = (UnitDetailsBlock) projectUpdated.getProjectBlockById(blockId);

        int maxId = 0;
        UnitDetailsTableEntry toReturn = null;
        for (UnitDetailsTableEntry unitDetailsTableEntry : block.getTableEntries()) {
            if (unitDetailsTableEntry.getId() > maxId) {
                toReturn = unitDetailsTableEntry;
            }
        }
        return toReturn;
    }

    /**
     * update an existing entry
     *
     * @return the updated entry
     */
    public UnitDetailsTableEntry updateUnitsEntry(Integer projectId, Integer blockId, Integer entryId,
            UnitDetailsTableEntry unitTableEntry) {
        Project project = projectService.get(projectId);
        UnitDetailsBlock block = (UnitDetailsBlock) project.getSingleBlockByTypeAndId(ProjectBlockType.UnitDetails, blockId);
        checkForLock(block);

        validateEntry(unitTableEntry);

        UnitDetailsTableEntry entryToUpdate = block.getEntry(entryId);
        if (entryToUpdate == null) {
            throw new ValidationException(String.format("Unable to find row with ID %d on block with ID %d", entryId, blockId));
        }

        entryToUpdate.merge(unitTableEntry);
        entryToUpdate.setModifiedOn(environment.now());
        entryToUpdate.setModifiedBy(userService.currentUser().getUsername());

        projectService.updateProject(project);
        return entryToUpdate;
    }

    void validateEntry(UnitDetailsTableEntry unitTableEntry) {
        Project project = get(unitTableEntry.getProjectId());
        TemplateTenureType templateTenureType = project.getTemplate().getTenureTypes().stream()
                .filter(t -> t.getTenureType().getId().equals(unitTableEntry.getTenureId())).findFirst().orElse(null);
        if (templateTenureType == null || templateTenureType.getMarketTypes().stream()
                .noneMatch(t -> t.getId().equals(unitTableEntry.getMarketType().getId()))) {
            throw new ValidationException("Incorrect market type for the given tenure");
        }

        if (!(CategoryValue.Category.Bedrooms.equals(unitTableEntry.getNbBeds().getCategory())
                && CategoryValue.Category.UnitTypes.equals(unitTableEntry.getUnitType().getCategory()))) {
            throw new ValidationException("Incorrect category assigned to table entry");
        }
    }

    public UnitDetailsMetaData getUnitDetailsMetaDataForProject(Integer projectId) {
        Project project = projectService.get(projectId);

        List<TemplateTenureType> tenureDetails = project.getTemplate().getTenureTypes().stream()
                .sorted(Comparator.comparingInt(TemplateTenureType::getDisplayOrder)).collect(Collectors.toList());

        UnitDetailsMetaData metaData = new UnitDetailsMetaData();
        metaData.setBeds(refDataService.findAllByCategoryOrderByDisplayOrder(CategoryValue.Category.Bedrooms));
        metaData.setUnitDetails(refDataService.findAllByCategoryOrderByDisplayOrder(CategoryValue.Category.UnitTypes));
        metaData.setTenureDetails(tenureDetails);
        return metaData;
    }

}
