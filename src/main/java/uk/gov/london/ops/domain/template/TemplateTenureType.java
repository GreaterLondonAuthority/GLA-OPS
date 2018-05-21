/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.domain.template;

import com.fasterxml.jackson.annotation.JsonIgnore;
import uk.gov.london.ops.domain.refdata.TenureType;

import javax.persistence.*;

/**
 * Created by chris on 12/10/2016.
 */
@Entity(name = "template_tenure_type")
public class TemplateTenureType {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tenure_type_seq_gen")
    @SequenceGenerator(name = "tenure_type_seq_gen", sequenceName = "tenure_typ_seq", initialValue = 10000, allocationSize = 1)
    private Integer id;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "external_id")
    private TenureType tenureType;

    @Column(name = "tariff_cap")
    private Integer tariffRate;

    @Column(name = "display_order")
    private Integer displayOrder;

    public TemplateTenureType() {}

    public TemplateTenureType(TenureType tenureType) {
        this.tenureType = tenureType;
    }

    public String getName() {
        return tenureType.getName();
    }

    public void setName(String name) {
    }

    public Integer getTariffRate() {
        return tariffRate;
    }

    public void setTariffRate(Integer tariffRate) {
        this.tariffRate = tariffRate;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getExternalId() {
        return tenureType.getId();
    }

    public void setExternalId(Integer externalId) {
        this.tenureType = new TenureType(externalId);
    }

    public TenureType getTenureType() {
        return tenureType;
    }

    public void setTenureType(TenureType tenureType) {
        this.tenureType = tenureType;
    }

    @JsonIgnore
    @Transient
    // this is sufficient for now, may need to make a persistent attribute eventually
    public boolean isZeroUnitEntry() {
        return tariffRate != null && tariffRate == 0;
    }
}
