/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.service.project;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.london.ops.domain.project.*;

@Service
@Transactional
public class ProjectGrantService extends BaseProjectService {

    public Project updateProjectCalculateGrant(Project project, CalculateGrantBlock block, boolean autosave) {
        checkForLock(project.getCalculateGrantBlock());
        project.getCalculateGrantBlock().merge(block);
        releaseOrRefreshLock(project.getCalculateGrantBlock(), !autosave);
        return this.updateProject(project);
    }

    public Project updateProjectNegotiatedGrant(Project project, NegotiatedGrantBlock block, boolean autosave) {
        checkForLock(project.getNegotiatedGrantBlock());
        project.getNegotiatedGrantBlock().merge(block);
        releaseOrRefreshLock(project.getNegotiatedGrantBlock(), !autosave);
        return this.updateProject(project);
    }

    public Project updateProjectDeveloperLedGrant(Project project, DeveloperLedGrantBlock block, boolean autosave) {
        checkForLock(project.getDeveloperLedGrantBlock());
        project.getDeveloperLedGrantBlock().merge(block);
        releaseOrRefreshLock(project.getDeveloperLedGrantBlock(), !autosave);
        return this.updateProject(project);
    }

    public Project updateProjectIndicativeGrant(Project project, IndicativeGrantBlock block, boolean autosave) {
        checkForLock(project.getIndicativeGrantBlock());
        project.getIndicativeGrantBlock().merge(block);
        releaseOrRefreshLock(project.getIndicativeGrantBlock(), !autosave);
        return this.updateProject(project);
    }

}
