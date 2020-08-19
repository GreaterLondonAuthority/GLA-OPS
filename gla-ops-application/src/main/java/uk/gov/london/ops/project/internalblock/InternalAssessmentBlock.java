/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.internalblock;

import static javax.persistence.CascadeType.ALL;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import uk.gov.london.ops.assessment.Assessment;
import uk.gov.london.ops.assessment.AssessmentStatus;
import uk.gov.london.ops.framework.jpa.Join;
import uk.gov.london.ops.framework.jpa.JoinData;

@Entity(name = "internal_assessment_block")
@DiscriminatorValue("ASSESSMENT")
@JoinData(sourceTable = "internal_assessment_block", sourceColumn = "id", targetTable = "internal_project_block",
        targetColumn = "id", joinType = Join.JoinType.OneToOne,
        comment = "the assessment block is a subclass of the internal project block and shares a common key")
public class InternalAssessmentBlock extends InternalProjectBlock {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "internal_assessment_block_seq_gen")
    @SequenceGenerator(name = "internal_assessment_block_seq_gen", sequenceName = "internal_assessment_block_seq",
            initialValue = 10000, allocationSize = 1)
    private Integer id;

    @JoinData(joinType = Join.JoinType.OneToMany, sourceColumn = "id", targetColumn = "block_id",
            targetTable = "assessment", comment = "")
    @OneToMany(fetch = FetchType.LAZY, cascade = ALL, orphanRemoval = true, mappedBy = "block", targetEntity = Assessment.class)
    @JsonIgnore
    private final List<Assessment> assessments = new ArrayList<>();

    @Override
    public Integer getId() {
        return id;
    }

    public List<Assessment> getAssessments() {
        return assessments;
    }

    public List<Assessment> getAssessmentsForDisplay() {
        return assessments.stream().filter(a -> !a.getStatus().equals(AssessmentStatus.Abandoned)).collect(Collectors.toList());
    }

    public void addAssessment(Assessment assessment) {
        assessment.setBlock(this);
        assessments.add(assessment);
    }

    @Override
    public InternalAssessmentBlock clone() {
        InternalAssessmentBlock clone = (InternalAssessmentBlock) super.clone();
        return clone;
    }

}
