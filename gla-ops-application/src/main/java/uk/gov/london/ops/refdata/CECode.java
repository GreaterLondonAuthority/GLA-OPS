/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.refdata;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import uk.gov.london.ops.framework.jpa.Join;
import uk.gov.london.ops.framework.jpa.JoinData;

@Entity(name = "ce_code")
public class CECode {

    @Id
    @Column(name = "id")
    private Integer id;

    @Column(name = "finance_category_id")
    @JoinData(targetTable = "finance_category", targetColumn = "id", joinType = Join.JoinType.OneToOne,
            comment = "This is the finance category code for the record")
    private Integer financeCategoryId;

    public CECode() {}

    public CECode(Integer id, Integer financeCategoryId) {
        this.id = id;
        this.financeCategoryId = financeCategoryId;
    }

    public CECode(Integer id) {
        this.id = id;
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
