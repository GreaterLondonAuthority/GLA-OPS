/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.skills;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;

@Entity(name = "contract_type_selection")
public class ContractTypeSelection {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "contract_type_selection_seq_gen")
    @SequenceGenerator(name = "contract_type_selection_seq_gen", sequenceName = "contract_type_selection_seq",
            initialValue = 10000, allocationSize = 1)
    private Integer id;

    @Column(name = "name")
    private String name;

    @Column(name = "selected")
    private Boolean selected;

    public ContractTypeSelection() {
    }

    public ContractTypeSelection(String name, Boolean selected) {
        this.name = name;
        this.selected = selected;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getSelected() {
        return selected;
    }

    public void setSelected(Boolean selected) {
        this.selected = selected;
    }

    public ContractTypeSelection clone() {
        ContractTypeSelection clone = new ContractTypeSelection();
        clone.setName(this.getName());
        clone.setSelected(this.getSelected());
        return clone;
    }


}
