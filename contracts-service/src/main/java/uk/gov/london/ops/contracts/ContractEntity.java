/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.contracts;

import uk.gov.london.ops.framework.enums.ContractWorkflowType;

import javax.persistence.*;
import java.io.Serializable;

@Entity(name = "contract")
public class ContractEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "contract_seq_gen")
    @SequenceGenerator(name = "contract_seq_gen", sequenceName = "contract_seq", initialValue = 100, allocationSize = 1)
    private Integer id;

    @Column
    private String name;

    @Column
    @Enumerated(EnumType.STRING)
    private ContractWorkflowType contractWorkflowType = ContractWorkflowType.SIGNED_TO_AUTHORISE_PAYMENTS;

    public ContractEntity() {
    }

    public ContractEntity(ContractModel model) {
        this.id = model.getId();
        this.name = model.getName();
        this.contractWorkflowType = model.getContractWorkflowType();
    }

    public ContractEntity(String name) {
        this.name = name;
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

    public ContractWorkflowType getContractWorkflowType() {
        return contractWorkflowType;
    }

    public void setContractWorkflowType(ContractWorkflowType contractWorkflowType) {
        this.contractWorkflowType = contractWorkflowType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ContractEntity contract = (ContractEntity) o;

        if (id != null ? !id.equals(contract.id) : contract.id != null) {
            return false;
        }
        return name != null ? name.equals(contract.name) : contract.name == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }

}
