/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.template.domain;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum TemplateBlockCommand {
    REMOVE_BLOCK("Remove Block", true,
            "Warning: by removing the [block_name] block,"
                    + " you will remove it from all projects using this template. Any data added to it to date will be "
                    + "hidden and can only be recovered by the central OPS team.",
            new String[]{TemplateBlockCommand.DISPLAY_NAME}),

    UPDATE_DISPLAY_NAME("Update Display Name", true, "Update Block Display Name",
            new String[]{TemplateBlockCommand.DISPLAY_NAME}, true, 150),

    UPDATE_INFO_MESSAGE("Update Info Message", true, "Update Block Info Message",
            null, true, 255),

    REMOVE_QUESTION("Remove Question", false,
            "Removing this question will remove both the question and all its answers to date from all projects using this template. These can only be recovered by the central OPS team.",
            null, true, 255),

    EDIT_MILESTONES("Edit Milestones", false),

    EDIT_LEARNING_GRANT_LABELS("Edit Learning Grant Labels", false),

    EDIT_USER_DEFINED_OUTPUT_BLOCK("Edit User Defined Output Configuration", false);

    public static final List<TemplateBlockCommand> GLOBAL_COMMANDS =
            Collections.unmodifiableList(
                    Arrays.stream(TemplateBlockCommand.values()).filter(e -> e.global).collect(Collectors.toList()));

    public static final String DISPLAY_NAME = "[block_name]";

    private String title;
    private boolean global = false;
    private String warningMessage;
    private String[] substitutions;
    private boolean requiresComment = false;
    private int maxCommentLength = 0;

    TemplateBlockCommand(String title, String warningMessage) {
        this.title = title;
        this.warningMessage = warningMessage;
    }

    TemplateBlockCommand(String title, boolean global) {
        this.title = title;
        this.global = global;
    }

    TemplateBlockCommand(String title, boolean global, String warningMessage) {
        this.title = title;
        this.global = global;
        this.warningMessage = warningMessage;
    }

    TemplateBlockCommand(String title, boolean global, String warningMessage, String[] substitutions) {
        this.title = title;
        this.global = global;
        this.warningMessage = warningMessage;
        this.substitutions = substitutions;
    }

    TemplateBlockCommand(String title, boolean global, String warningMessage, String[] substitutions, boolean requiresComment,
            int maxCommentLength) {
        this.title = title;
        this.global = global;
        this.warningMessage = warningMessage;
        this.substitutions = substitutions;
        this.requiresComment = requiresComment;
        this.maxCommentLength = maxCommentLength;
    }

    public String getTitle() {
        return title;
    }

    public String getName() {
        return this.name();
    }

    public boolean isGlobal() {
        return global;
    }

    public String getWarningMessage() {
        return warningMessage;
    }

    public String[] getSubstitutions() {
        return substitutions;
    }

    public boolean isRequiresComment() {
        return requiresComment;
    }

    public int getMaxCommentLength() {
        return maxCommentLength;
    }
}
