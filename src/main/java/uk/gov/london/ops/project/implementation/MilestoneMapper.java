/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.implementation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.london.ops.Environment;
import uk.gov.london.ops.domain.project.Milestone;
import uk.gov.london.ops.domain.template.MilestoneTemplate;
import uk.gov.london.ops.domain.template.Template;
import uk.gov.london.ops.service.UserService;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;

@Component
public class MilestoneMapper {

    Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    UserService userService;

    @Autowired
    Environment environment;

    public Set<Milestone> toProjectMilestones(Set<MilestoneTemplate> milestones, Template template) {
        return toProjectMilestones(milestones, template, userService.currentUser().getUsername(), environment.now());
    }

    public Set<Milestone> toProjectMilestones(Set<MilestoneTemplate> milestones, Template template, String createdBy, OffsetDateTime createdOn) {
        Set<Milestone> projectMilestones = new HashSet<>();
        if (milestones != null && milestones.size() > 0) {
            for (MilestoneTemplate milestone : milestones) {
                Milestone projectMilestone = new Milestone();
                projectMilestone.setExternalId(milestone.getExternalId());
                projectMilestone.setMonetarySplit(milestone.getMonetarySplit() == null ? 0 : milestone.getMonetarySplit());
                projectMilestone.setSummary(milestone.getSummary());
                projectMilestone.setRequirement(milestone.getRequirement());
                projectMilestone.setDisplayOrder(milestone.getDisplayOrder());
                // an empty value is assumed to be true
                projectMilestone.setMonetary(milestone.getMonetary() == null || milestone.getMonetary());
                projectMilestone.setKeyEvent(milestone.isKeyEvent());
                projectMilestone.setNaSelectable(milestone.isNaSelectable());

                if (projectMilestone.getMonetary() && !template.getAllowMonetaryMilestones()) {
                    if ((environment != null) && !environment.initTestData()) {
                        // Don't show this warning if we're probably working with test data
                        log.warn("Monetary milestone included in template that should not allow it: overriding (templateID  = {})", template.getId());
                    }
                    projectMilestone.setMonetary(false);
                }

                projectMilestone.setCreatedOn(createdOn);
                projectMilestone.setCreatedBy(createdBy);
                projectMilestones.add(projectMilestone);
            }
        }
        return projectMilestones;
    }
}
