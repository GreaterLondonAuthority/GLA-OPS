/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.repeatingentity;

import static javax.persistence.CascadeType.ALL;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import uk.gov.london.ops.framework.enums.Requirement;
import uk.gov.london.ops.framework.exception.ValidationException;
import uk.gov.london.ops.framework.jpa.Join;
import uk.gov.london.ops.framework.jpa.JoinData;
import uk.gov.london.ops.project.block.ProjectBlockType;
import uk.gov.london.ops.project.template.domain.RepeatingEntityTemplateBlock;
import uk.gov.london.ops.project.template.domain.TemplateBlock;
import uk.gov.london.ops.project.template.domain.UserDefinedOutputTemplateBlock;

/**
 * Created by carmina on 26/11/2019.
 */
@Entity(name = "user_defined_outputs")
@DiscriminatorValue("USER_DEFINED_OUTPUTS")
@JoinData(sourceTable = "user_defined_outputs", sourceColumn = "id", targetTable = "project_block",
        targetColumn = "id", joinType = Join.JoinType.OneToOne,
        comment = "the user defined outputs block is a subclass of the project block and shares a common key")
public class UserDefinedOutputBlock extends RepeatingEntityBlock<UserDefinedOutput> {

    @JoinData(joinType = Join.JoinType.OneToMany, sourceColumn = "id", targetColumn = "block_id",
            targetTable = "user_defined_output", comment = "")
    @OneToMany(fetch = FetchType.LAZY, cascade = ALL, orphanRemoval = true)
    @JoinColumn(name = "block_id")
    @OrderBy("id")
    List<UserDefinedOutput> userDefinedOutputs = new ArrayList<>();

    @Override
    public ProjectBlockType getProjectBlockType() {
        return ProjectBlockType.UserDefinedOutput;
    }

    @Override
    public List<UserDefinedOutput> getRepeatingEntities() {
        return userDefinedOutputs;
    }

    @Override
    public String getRootPath() {
        return "userDefinedOutput";
    }

    public List<UserDefinedOutput> getUserDefinedOutputs() {
        return userDefinedOutputs;
    }

    public void setUserDefinedOutputs(List<UserDefinedOutput> userDefinedOutputs) {
        this.userDefinedOutputs = userDefinedOutputs;
    }

    @Override
    protected void generateValidationFailures() {
        if (userDefinedOutputs != null && (userDefinedOutputs.isEmpty())) {
            this.addErrorMessage("table", "", "You must add at least minimum number of outputs");
        }
    }

    @Override
    public UserDefinedOutput getNewEntityInstance() {
        return new UserDefinedOutput();
    }

    @Override
    public boolean isComplete() {
        boolean hasMissingBaseline = false;
        boolean incorrectNumberOfEntities = false;
        UserDefinedOutputTemplateBlock blockTemplate = (UserDefinedOutputTemplateBlock) project.getTemplate()
                .getSingleBlockByType(ProjectBlockType.UserDefinedOutput);

        if (blockTemplate != null) {
            if (blockTemplate.getBaselineRequirement() == Requirement.mandatory) {
                hasMissingBaseline = getUserDefinedOutputs().stream()
                        .anyMatch(udo -> udo.getBaseline() == null || udo.getBaseline().isEmpty());
            }

            if (blockTemplate.getMinNumberOfEntities() != null && blockTemplate.getMaxNumberOfEntities() != null) {
                incorrectNumberOfEntities = getUserDefinedOutputs().size() < blockTemplate.getMinNumberOfEntities()
                        || getUserDefinedOutputs().size() > blockTemplate.getMaxNumberOfEntities();
            }
        }
        return isNotRequired() || (super.isComplete() && !hasMissingBaseline && !incorrectNumberOfEntities);
    }
}
