/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.service.project;

import uk.gov.london.ops.domain.project.Project;

/**
 * Any Service that needs notification of a block being cloned can implement this interface and it will be picked
 * up by project service, and called once the cloning has been completed
 * Created by chris on 10/07/2017.
 */
public interface PostCloneNotificationListener {

    /**
     * Implement to be informed a block has been cloned.
     * @param project - project should be checked ot ensure relevant block is present
     * @param originalBlockId - source block
     * @param newBlockId - newly cloned version of block
     */
    void handleBlockClone(Project project, Integer originalBlockId, Integer newBlockId);

    /**
     * Implement to be informed a project has been cloned
     * @param oldProject - project should be checked to ensure relevant block is present
     * @param originalBlockId - source block
     * @param newProject - project should be checked to ensure relevant block is present
     * @param newBlockId - newly cloned version of block on new project
     */
    void handleProjectClone(Project oldProject, Integer originalBlockId, Project newProject, Integer newBlockId);

}
