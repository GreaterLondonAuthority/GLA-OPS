/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.implementation;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.london.ops.framework.Environment;
import uk.gov.london.ops.project.Project;
import uk.gov.london.ops.project.block.NamedProjectBlock;
import uk.gov.london.ops.project.state.ProjectStatus;
import uk.gov.london.ops.user.UserService;

@Aspect
@Component
/**
 * AOP Component to persist project completeness recalculations on save/all
 */
public class UpdateProjectAspect {

    @Autowired
    UserService userService;

    @Autowired
    Environment environment;

    @Before("execution(* uk.gov.london.ops.project.implementation.repository.ProjectRepository+.save(..))")
    public void updateEntityChangeOnSave(JoinPoint joinPoint) {
        updateEntityChange((Project) joinPoint.getArgs()[0]);
    }

    @Before("execution(* uk.gov.london.ops.project.implementation.repository.ProjectRepository+.saveAndFlush(..))")
    public void updateEntityChangeOnSaveAndFlush(JoinPoint joinPoint)  {
        updateEntityChange((Project) joinPoint.getArgs()[0]);
    }

    @SuppressWarnings("unchecked")
    @Before("execution(* uk.gov.london.ops.project.implementation.repository.ProjectRepository+.saveAll(..))")
    public void updateEntityChangeOnSaveAll(JoinPoint joinPoint)  {
        updateEntityChanges((Iterable<Project>) joinPoint.getArgs()[0]);
    }

    void updateEntityChanges(Iterable<Project> projects)  {
        for (Project project : projects) {
            updateEntityChange(project);
        }
    }
    void updateEntityChange(Project  project)  {
        project.recalculateProjectGrantEligibility();
        for (NamedProjectBlock projectBlock : project.getLatestProjectBlocks()) {
            projectBlock.resetErrorMessages();
            if (projectBlock.isAbleToPersistIsComplete()) {
                projectBlock.setBlockMarkedComplete(projectBlock.isComplete());
            } else {
                projectBlock.setBlockMarkedComplete(null);
            }
        }
        if (project.isEnriched()) {
            for (NamedProjectBlock projectBlock : project.getLatestProjectBlocks()) {
                Boolean hasUpdatesPersisted = projectBlock.hasUpdates();
                projectBlock.setHasUpdatesPersisted(hasUpdatesPersisted);
            }
            if (ProjectStatus.Active.equals(project.getStatusType())) {
                project.setApprovalWillGeneratePaymentPersisted(project.getApprovalWillCreatePendingPayment());
                project.setApprovalWillGenerateReclaimPersisted(project.getApprovalWillCreatePendingReclaim());
            } else {
                project.setApprovalWillGeneratePaymentPersisted(false);
                project.setApprovalWillGenerateReclaimPersisted(false);
            }
        }
    }
}
