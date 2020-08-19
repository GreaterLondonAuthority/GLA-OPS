/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.framework;

import uk.gov.london.ops.organisation.model.Organisation;
import uk.gov.london.ops.project.Project;
import uk.gov.london.ops.project.state.ProjectState;
import uk.gov.london.ops.user.domain.User;

public class BasicTextReplacer {

    public static final String PROJECT_ID_SUBSTITUTION = "PROJECT_ID";
    public static final String ORGANISATION_ID_SUBSTITUTION = "ORGANISATION_ID";
    public static final String ORGANISATION_NAME_SUBSTITUTION = "ORGANISATION_NAME";
    public static final String USERS_NAME_SUBSTITUTION = "USERS_NAME";
    public static final String FROM_STATUS = "FROM_STATUS";
    public static final String TO_STATUS = "TO_STATUS";
    public static final String PREFIX = "{";
    public static final String SUFFIX = "}";


    public static String replaceText(String text, User user, Project project, Organisation organisation) {
        text = text.replace(PREFIX + PROJECT_ID_SUBSTITUTION + SUFFIX, "P" + project.getId());
        text = text.replace(PREFIX + ORGANISATION_ID_SUBSTITUTION + SUFFIX, String.valueOf(organisation.getId()));
        text = text.replace(PREFIX + ORGANISATION_NAME_SUBSTITUTION + SUFFIX, String.valueOf(organisation.getName()));
        text = text.replace(PREFIX + USERS_NAME_SUBSTITUTION + SUFFIX, String.valueOf(user.getFullName()));

        return text;
    }

    public static String replaceText(String text, User user, Project project, Organisation organisation, ProjectState fromState, ProjectState toState) {
        text = BasicTextReplacer.replaceText(text, user, project, organisation);
        text = text.replace(PREFIX + FROM_STATUS + SUFFIX, fromState.getStatus());
        text = text.replace(PREFIX + TO_STATUS + SUFFIX, toState.getStatus());
        return text;
    }

}
