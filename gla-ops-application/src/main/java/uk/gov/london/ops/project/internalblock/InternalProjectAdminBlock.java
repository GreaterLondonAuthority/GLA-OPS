/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.internalblock;

import uk.gov.london.ops.framework.jpa.Join;
import uk.gov.london.ops.framework.jpa.JoinData;

import javax.persistence.*;

@Entity(name = "internal_project_admin_block")
@DiscriminatorValue("PROJECT_ADMIN")
@JoinData(sourceTable = "internal_project_admin_block", sourceColumn = "id", targetTable = "internal_project_block",
        targetColumn = "id", joinType = Join.JoinType.OneToOne,
        comment = "the internal project admin block is a subclass of the internal project block and shares a common key")
public class InternalProjectAdminBlock extends InternalProjectBlock {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "internal_project_admin_block_seq_gen")
    @SequenceGenerator(name = "internal_project_admin_block_seq_gen", sequenceName = "internal_project_admin_block_seq",
        initialValue = 10000, allocationSize = 1)
    private Integer id;

    @Column(name = "project_short_name")
    private String projectShortName;

    @Column(name = "organisation_short_name")
    private String organisationShortName;

    public InternalProjectAdminBlock() {
        setType(InternalBlockType.ProjectAdmin);
    }

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public void setId(Integer id) {
        this.id = id;
    }

    public String getProjectShortName() {
        return projectShortName;
    }

    public void setProjectShortName(String projectShortName) {
        this.projectShortName = projectShortName;
    }

    public String getOrganisationShortName() {
        return organisationShortName;
    }

    public void setOrganisationShortName(String organisationShortName) {
        this.organisationShortName = organisationShortName;
    }

    @Override
    public InternalProjectBlock clone() {
        InternalProjectAdminBlock clone = (InternalProjectAdminBlock) super.clone();
        clone.setProjectShortName(this.projectShortName);
        clone.setOrganisationShortName(this.organisationShortName);
        return clone;
    }

    @Override
    public String merge(InternalProjectBlock updated) {
        InternalProjectAdminBlock other = (InternalProjectAdminBlock) updated;
        setProjectShortName(other.projectShortName);
        setOrganisationShortName(other.organisationShortName);
        return null;
    }

}
