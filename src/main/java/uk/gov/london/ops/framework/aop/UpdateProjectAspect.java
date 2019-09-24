/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.framework.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.london.ops.Environment;
import uk.gov.london.ops.domain.project.NamedProjectBlock;
import uk.gov.london.ops.domain.project.Project;
import uk.gov.london.ops.service.UserService;

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

    @Before("execution(* uk.gov.london.ops.repository.ProjectRepository+.save(..))")
    public void updateEntityChangeOnSave(JoinPoint joinPoint) {
        updateEntityChange((Project) joinPoint.getArgs()[0]);
    }

    @Before("execution(* uk.gov.london.ops.repository.ProjectRepository+.saveAndFlush(..))")
    public void updateEntityChangeOnSaveAndFlush(JoinPoint joinPoint)  {
        updateEntityChange((Project) joinPoint.getArgs()[0]);
    }

    @SuppressWarnings("unchecked")
    @Before("execution(* uk.gov.london.ops.repository.ProjectRepository+.saveAll(..))")
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
    }
}
