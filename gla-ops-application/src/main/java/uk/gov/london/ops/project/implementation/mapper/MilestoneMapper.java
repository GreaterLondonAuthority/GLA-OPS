/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.implementation.mapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.london.ops.framework.environment.Environment;
import uk.gov.london.ops.project.milestone.Milestone;
import uk.gov.london.ops.project.template.domain.MilestoneTemplate;
import uk.gov.london.ops.project.template.domain.Template;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;

import static uk.gov.london.ops.framework.OPSUtils.currentUsername;

@Component
public class MilestoneMapper {

    Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    Environment environment;

    public Set<Milestone> toProjectMilestones(Set<MilestoneTemplate> milestones, Template template) {
        return toProjectMilestones(milestones, template, currentUsername(), environment.now());
    }

    public Set<Milestone> toProjectMilestones(Set<MilestoneTemplate> templateMilestones, Template template, String createdBy,
                                              OffsetDateTime createdOn) {
        Set<Milestone> projectMilestones = new HashSet<>();
        if (templateMilestones != null && templateMilestones.size() > 0) {
            for (MilestoneTemplate templateMilestone : templateMilestones) {
                Milestone projectMilestone = toProjectMilestone(templateMilestone, template, createdBy, createdOn);
                projectMilestones.add(projectMilestone);
            }
        }
        return projectMilestones;
    }

    public Milestone toProjectMilestone(MilestoneTemplate templateMilestone, Template template, String createdBy,
                                        OffsetDateTime createdOn) {
        Milestone projectMilestone = new Milestone();
        projectMilestone.setExternalId(templateMilestone.getExternalId());
        projectMilestone.setMonetarySplit(templateMilestone.getMonetarySplit() == null ? 0 : templateMilestone.getMonetarySplit());
        projectMilestone.setSummary(templateMilestone.getSummary());
        projectMilestone.setRequirement(templateMilestone.getRequirement());
        projectMilestone.setDisplayOrder(templateMilestone.getDisplayOrder());
        // an empty value is assumed to be true
        projectMilestone.setMonetary(templateMilestone.getMonetary() == null || templateMilestone.getMonetary());
        projectMilestone.setKeyEvent(templateMilestone.isKeyEvent());
        projectMilestone.setNaSelectable(templateMilestone.isNaSelectable());
        if (projectMilestone.getMonetary() && !template.getAllowMonetaryMilestones()) {
            if ((environment != null) && !environment.initTestData()) {
                // Don't show this warning if we're probably working with test data
                log.warn("Monetary milestone included in template that should not allow it: overriding (templateID  = {})",
                        template.getId());
            }
            projectMilestone.setMonetary(false);
        }
        projectMilestone.setCreatedOn(createdOn);
        projectMilestone.setCreatedBy(createdBy);
        return projectMilestone;
    }

}
