/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.domain.project;

import com.fasterxml.jackson.annotation.JsonIgnore;
import uk.gov.london.ops.domain.OpsEntity;
import uk.gov.london.ops.domain.outputs.OutputCategoryConfiguration;
import uk.gov.london.ops.domain.outputs.OutputType;
import uk.gov.london.ops.util.jpajoins.Join;
import uk.gov.london.ops.util.jpajoins.JoinData;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static uk.gov.london.ops.domain.project.OutputTableEntry.Source.WebUI;

/**
 * Stores the output table details
 *
 * Created by chris on 11/01/2017.
 */
@Entity(name = "OUTPUT_TABLE_ENTRY")
public class OutputTableEntry implements OpsEntity<Integer> {

    public static enum Source{PCS, WebUI}

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "output_seq_gen")
    @SequenceGenerator(name = "output_seq_gen", sequenceName = "output_seq", initialValue = 10000, allocationSize = 1)
    private Integer id;

    @Column(name = "project_id")
    @JsonIgnore
    @JoinData(targetTable = "project", targetColumn = "id", joinType = Join.JoinType.OneToOne,
            comment = "The project for this entry")
    protected Integer projectId;

    @Column(name = "block_id")
    @JsonIgnore
    @JoinData(targetTable = "project_block", targetColumn = "id", joinType = Join.JoinType.OneToOne,
            comment = "The block id for this record. These records will be duplicated per block.")
    protected Integer blockId;

    @Column(name = "year")
    private Integer year;

    @Column(name = "month")
    private Integer month;

    @Column(name = "year_month")
    @JsonIgnore
    private Integer yearMonth;

    @Column(name = "forecast")
    private BigDecimal forecast;

    @Column(name = "actual")
    private BigDecimal actual;

    @Column(name = "created_on")
    private OffsetDateTime createdOn;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "modified_on")
    private OffsetDateTime modifiedOn;

    @Column(name = "modified_by")
    private String modifiedBy;

    @OneToOne(cascade = {}, optional = false, targetEntity = OutputCategoryConfiguration.class)
    @JoinColumn(name = "configuration_id")
    private OutputCategoryConfiguration config;


    @OneToOne(cascade = {})
    @JoinColumn(name = "output_type")
    private OutputType outputType;


    @Column(name="source")
    @Enumerated(EnumType.STRING)
    private Source source;

    public OutputTableEntry() {
        this.source = WebUI;
    }

    public OutputTableEntry(Integer projectId,
                            Integer blockId,
                            OutputCategoryConfiguration config,
                            OutputType outputType,
                            Integer year,
                            Integer month,
                            BigDecimal forecast,
                            BigDecimal actual) {
        this(projectId, blockId, config, outputType, year, month, forecast, actual, WebUI);
    }

    public OutputTableEntry(Integer projectId,
                            Integer blockId,
                            OutputCategoryConfiguration config,
                            OutputType outputType,
                            Integer year,
                            Integer month,
                            BigDecimal forecast,
                            BigDecimal actual,
                            Source source) {
        this.projectId = projectId;
        this.blockId = blockId;
        this.config = config;
        this.outputType = outputType;
        this.year = year;
        this.month = month;
        this.forecast = forecast;
        this.actual = actual;
        this.source = source;
        updateYearMonth();
    }

    @Override
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getProjectId() {
        return projectId;
    }

    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }

    public Integer getBlockId() {
        return blockId;
    }

    public void setBlockId(Integer blockId) {
        this.blockId = blockId;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
        updateYearMonth();
    }

    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
        updateYearMonth();
    }

    private void updateYearMonth() {
        if (getYear() != null && getMonth() != null) {
            yearMonth = Integer.valueOf(String.format("%d%02d", getYear(), getMonth()));
        } else if (getYear() != null) {
            // if no month then assume start of financial year
            yearMonth = Integer.valueOf(String.format("%d04", getYear()));
        }
    }

    public Integer getYearMonth() {
        return yearMonth;
    }

    public void setYearMonth(Integer yearMonth) {
        this.yearMonth = yearMonth;
    }


    public BigDecimal getForecast() {
        return forecast;
    }

    public void setForecast(BigDecimal forecast) {
        this.forecast = forecast;
    }

    public BigDecimal getActual() {
        return actual;
    }

    public void setActual(BigDecimal actual) {
        this.actual = actual;
    }

    public Source getSource() {
        return source;
    }

    public void setSource(Source source) {
        this.source = source;
    }

    @Override
    public OffsetDateTime getCreatedOn() {
        return createdOn;
    }

    @Override
    public void setCreatedOn(OffsetDateTime createdOn) {
        this.createdOn = createdOn;
    }

    @Override
    public String getCreatedBy() {
        return createdBy;
    }

    @Override
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    @Override
    public OffsetDateTime getModifiedOn() {
        return modifiedOn;
    }

    @Override
    public void setModifiedOn(OffsetDateTime modifiedOn) {
        this.modifiedOn = modifiedOn;
    }

    @Override
    public String getModifiedBy() {
        return modifiedBy;
    }

    @Override
    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    public OutputCategoryConfiguration getConfig() {
        return config;
    }

    public void setConfig(OutputCategoryConfiguration config) {
        this.config = config;
    }

    public OutputType getOutputType() {
        return outputType;
    }

    public void setOutputType(OutputType outputType) {
        this.outputType = outputType;
    }

    @Transient
    public BigDecimal getDifference() {
        if (forecast == null || actual == null) {
            return null;
        }
        return actual.subtract(forecast);
    }


}

