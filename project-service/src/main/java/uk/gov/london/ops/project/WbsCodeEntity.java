/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import uk.gov.london.ops.framework.ComparableItem;

@Entity(name = "wbs_code")
public class WbsCodeEntity implements ComparableItem {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "wbs_code_seq_gen")
    @SequenceGenerator(name = "wbs_code_seq_gen", sequenceName = "wbs_code_seq", initialValue = 10000, allocationSize = 1)
    private Integer id;

    @Column(name = "code")
    private String code;

    @Column(name = "wbs_type")
    private String type;

    public WbsCodeEntity() {
    }

    public WbsCodeEntity(String code) {
        this.code = code;
    }

    public WbsCodeEntity(String code, String type) {
        this.code = code;
        this.type = type;
    }

    public WbsCodeEntity(Integer id, String code, String type) {
        this.id = id;
        this.code = code;
        this.type = type;
    }

    public WbsCodeEntity copy() {
        return new WbsCodeEntity(this.getCode(), this.getType());
    }

    public Integer getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        WbsCodeEntity wbsCode = (WbsCodeEntity) o;

        if (code != null ? !code.equals(wbsCode.code) : wbsCode.code != null) {
            return false;
        }
        return !(type != null ? !type.equals(wbsCode.type) : wbsCode.type != null);

    }

    @Override
    public int hashCode() {
        int result = code != null ? code.hashCode() : 0;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        return result;
    }

    @Override
    public String getComparisonId() {
        return code + ":" + type;
    }
}
