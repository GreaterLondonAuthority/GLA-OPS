/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.template.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Transient;
import uk.gov.london.ops.framework.jpa.Join;
import uk.gov.london.ops.framework.jpa.JoinData;
import uk.gov.london.ops.refdata.MarketType;
import uk.gov.london.ops.refdata.MarketTypeDTO;
import uk.gov.london.ops.refdata.TenureType;

/**
 * Created by chris on 12/10/2016.
 */
@Entity(name = "template_tenure_type")
public class TemplateTenureType implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tenure_type_seq_gen")
    @SequenceGenerator(name = "tenure_type_seq_gen", sequenceName = "tenure_typ_seq",
            initialValue = 10000, allocationSize = 1)
    private Integer id;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "external_id")
    private TenureType tenureType;

    @Column(name = "tariff_cap")
    private Integer tariffRate;

    // TODO: Move to ProjectTenureDetails (actual ProjectTenureDetails)
    @Column(name = "display_order")
    private Integer displayOrder;

    @JoinData(sourceTable = "template_tenure_type", joinType = Join.JoinType.Complex,
            comment = "Inverse of join table relationship")
    @OneToMany(mappedBy = "templateTenureType", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<TemplateTenureTypeMarketType> templateTenureTypeMarketTypes = new HashSet<>();

    public TemplateTenureType() {
    }

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

    public Set<TemplateTenureTypeMarketType> getTemplateTenureTypeMarketTypes() {
        return templateTenureTypeMarketTypes;
    }

    public void setTemplateTenureTypeMarketTypes(Set<TemplateTenureTypeMarketType> templateTenureTypeMarketTypes) {
        this.templateTenureTypeMarketTypes = templateTenureTypeMarketTypes;
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public List<MarketTypeDTO> getMarketTypes() {
        List<MarketTypeDTO> marketTypeDTOs = new ArrayList<>();

        if (this.getTemplateTenureTypeMarketTypes() == null || this.getTemplateTenureTypeMarketTypes().isEmpty()) {
            if (tenureType != null && tenureType.getMarketTypes() != null) {
                List<MarketType> marketTypes = tenureType.getMarketTypes();
                for (MarketType marketType : marketTypes) {
                    marketTypeDTOs.add(new MarketTypeDTO(marketType));
                }
            }
        } else {
            for (TemplateTenureTypeMarketType templateTenureTypeMarketType : this.getTemplateTenureTypeMarketTypes()) {
                MarketTypeDTO dto = new MarketTypeDTO(templateTenureTypeMarketType.getMarketType(),
                        templateTenureTypeMarketType.getMarketTypeDisplayName());
                marketTypeDTOs.add(dto);
            }
        }
        return marketTypeDTOs;
    }

}
