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
import java.math.BigDecimal;

@Entity(name = "contract_type_funding_entry")
public class ContractTypeFundingEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "contract_type_funding_entry_seq_gen")
    @SequenceGenerator(name = "contract_type_funding_entry_seq_gen", sequenceName = "contract_type_funding_entry_seq",
            initialValue = 10000, allocationSize = 1)
    private Integer id;

    @Column(name = "contract_type")
    private String contractType;

    @Column(name = "contract_value")
    private BigDecimal contractValue;

    @Column(name = "flexible_allocation")
    private BigDecimal flexibleAllocation;

    public ContractTypeFundingEntry() {
    }

    public ContractTypeFundingEntry(String contractType, BigDecimal contractValue, BigDecimal flexibleAllocation) {
        this.contractType = contractType;
        this.contractValue = contractValue;
        this.flexibleAllocation = flexibleAllocation;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getContractType() {
        return contractType;
    }

    public void setContractType(String contractType) {
        this.contractType = contractType;
    }

    public BigDecimal getContractValue() {
        return contractValue;
    }

    public void setContractValue(BigDecimal contractValue) {
        this.contractValue = contractValue;
    }

    public BigDecimal getFlexibleAllocation() {
        return flexibleAllocation;
    }

    public void setFlexibleAllocation(BigDecimal flexibleAllocation) {
        this.flexibleAllocation = flexibleAllocation;
    }

    public ContractTypeFundingEntry clone() {
        ContractTypeFundingEntry clone = new ContractTypeFundingEntry();
        clone.setContractType(this.getContractType());
        clone.setContractValue(this.getContractValue());
        clone.setFlexibleAllocation(this.getFlexibleAllocation());
        return clone;
    }

    public boolean isComplete() {
        return contractValue != null && flexibleAllocation != null;
    }

    public boolean isModified() {
        return contractValue != null || flexibleAllocation != null;
    }

    public boolean isEmpty() {
        return contractValue == null && flexibleAllocation == null;
    }
}
