/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.domain.template;

import uk.gov.london.ops.util.jpajoins.NonJoin;

import javax.persistence.*;
import java.util.Set;

@Entity
public class ProcessingRoute {

    public static final String DEFAULT_PROCESSING_ROUTE_NAME = "default";
    public static final int DEFAULT_PROCESSING_ROUTE_EXT_ID = 999;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "processing_route_seq_gen")
    @SequenceGenerator(name = "processing_route_seq_gen", sequenceName = "processing_route_seq", initialValue = 10000, allocationSize = 1)
    private Integer id;

    @Column(name = "external_id")
    @NonJoin("External ID is just a unique key to identify similar processing routes across templates.")
    private Integer externalId;

    @Column(name = "name")
    private String name;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, targetEntity = MilestoneTemplate.class)
    @JoinColumn(name="processing_route_id")
    private Set<MilestoneTemplate> milestones;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getExternalId() {
        return externalId;
    }

    public void setExternalId(Integer externalId) {
        this.externalId = externalId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<MilestoneTemplate> getMilestones() {
        return milestones;
    }

    public void setMilestones(Set<MilestoneTemplate> milestones) {
        this.milestones = milestones;
    }

    public MilestoneTemplate getMilestoneByExternalId(Integer milestoneId) {
        for (MilestoneTemplate milestoneTemplate: milestones) {
            if (milestoneId.equals(milestoneTemplate.getExternalId())) {
                return milestoneTemplate;
            }
        }
        return null;
    }

}
