/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.question;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.london.ops.framework.exception.ValidationException;
import uk.gov.london.ops.project.BaseProjectService;
import uk.gov.london.ops.project.Project;
import uk.gov.london.ops.project.ProjectService;
import uk.gov.london.ops.project.implementation.repository.ProjectBlockRepository;
import uk.gov.london.ops.user.domain.User;

/**
 * Service interface for managing projects questions blocks.
 *
 * @author Chris Melville
 */
@Service
@Transactional
public class ProjectQuestionsService extends BaseProjectService {

    Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    ProjectService projectService;

    @Autowired
    private ProjectBlockRepository projectBlockRepository;

    public ProjectQuestionsBlock getQuestionsBlock(Integer blockId) {
        return (ProjectQuestionsBlock) projectBlockRepository.findById(blockId)
                .orElseThrow(() -> new ValidationException("not found"));
    }

    public ProjectQuestionsBlock updateProjectAnswers(Integer projectId, Integer blockId, ProjectQuestionsBlock answers,
            boolean autosave) {
        ProjectQuestionsBlock block = getQuestionsBlock(blockId);
        User user = userService.currentUser();
        log.trace("updateProjectAnswers start: projectID: {} user: {}", projectId, user);

        checkForLock(block);
        block.merge(answers);
        releaseOrRefreshLock(block, !autosave);

        ProjectQuestionsBlock updated = (ProjectQuestionsBlock) projectService.updateProjectBlock(block, projectId);

        long time = System.nanoTime();
        log.trace("({}ms) updateProjectAnswers end: projectID: {} user: {}", System.nanoTime() - time, projectId,
                user.getUsername());
        return updated;

    }

    public List<Project> findAllForQuestionId(int questionId) {
        return projectRepository.findAllForQuestion(questionId);
    }

    public Integer countByQuestion(Integer questionId) {
        return projectRepository.countByQuestion(questionId);
    }

}
