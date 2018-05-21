/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.domain.refdata;

import uk.gov.london.ops.util.jpajoins.Join;
import uk.gov.london.ops.util.jpajoins.JoinData;

import javax.persistence.*;

@Entity(name = "ce_code")
public class CECode {

    @Id
    @Column(name = "id")
    private Integer id;

    @Column(name = "finance_category_id")
    @JoinData(targetTable = "finance_category", targetColumn = "id", joinType = Join.JoinType.OneToOne, comment = "This is the finance category code for the record")
    private Integer financeCategoryId;

    public CECode() {}

    public CECode(Integer id, Integer financeCategoryId) {
        this.id = id;
        this.financeCategoryId = financeCategoryId;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getFinanceCategoryId() {
        return financeCategoryId;
    }

    public void setFinanceCategoryId(Integer financeCategoryId) {
        this.financeCategoryId = financeCategoryId;
    }

}
