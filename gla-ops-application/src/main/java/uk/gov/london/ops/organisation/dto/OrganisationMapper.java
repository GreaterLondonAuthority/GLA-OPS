/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.organisation.dto;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import uk.gov.london.ops.organisation.Organisation;
import uk.gov.london.ops.organisation.OrganisationStatus;
import uk.gov.london.ops.organisation.OrganisationType;
import uk.gov.london.ops.organisation.model.Address;
import uk.gov.london.ops.organisation.model.BulkUploadSummary;
import uk.gov.london.ops.organisation.model.OrganisationEntity;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

@Component
public class OrganisationMapper {

    private static final String AREA_LEAD       = "Area Lead";
    private static final String TEAM            = "Team";
    private static final String ORG_NAME        = "Org.Name";
    private static final String ACTIVE          = "Active";
    private static final String PROVIDER_NO     = "Provider No.";
    private static final String GAPS            = "Gaps";
    private static final String SAP_NUM         = "SAP Vendor No";
    private static final String ENTITY_TYPE     = "Entity Type";
    private static final String EMAIL           = "Email";
    private static final String ADDRESS_1       = "Address 1";
    private static final String ADDRESS_2       = "Add 2";
    private static final String ADDRESS_3       = "Add 3";
    private static final String ADDRESS_4       = "Add 4";
    private static final String ADDRESS_5       = "Add 5";
    private static final String POSTCODE        = "Postcode";
    private static final String CEO_TITLE       = "CEO Title";
    private static final String CEO_NAME        = "CEO Name";
    private static final String REGULATED       = "Regulated?";
    private static final String VIABILITY_SCORE = "Viability Score";
    private static final String GOV_SCORE       = "Gov. Score";
    private static final String ID              = "ID no.";
    private static final String STATUS          = "Status";
    private static final String[] HEADERS = new String[] {
            AREA_LEAD, TEAM, ORG_NAME, ACTIVE, PROVIDER_NO, GAPS, SAP_NUM, ENTITY_TYPE, EMAIL,
            ADDRESS_1, ADDRESS_2, ADDRESS_3, ADDRESS_4, ADDRESS_5, POSTCODE,
            CEO_TITLE, CEO_NAME, REGULATED, VIABILITY_SCORE, GOV_SCORE, ID, STATUS
    };
    Logger log = LoggerFactory.getLogger(getClass());

    public List<OrganisationEntity> toEntities(InputStream csvInputStream, BulkUploadSummary summary) throws IOException {
        List<CSVRecord> records = CSVFormat.DEFAULT.withNullString("").withHeader(HEADERS).withSkipHeaderRecord(true)
                .withTrim().parse(new InputStreamReader(csvInputStream, UTF_8)).getRecords();

        summary.setSourceRows(records.size());

        List<OrganisationEntity> entities = new ArrayList<>();
        for (CSVRecord record: records) {
            try {
                if (StringUtils.isEmpty(record.get(ORG_NAME))) {
                    log.warn("skipping row {} without organisation name", record.getRecordNumber());
                } else if (!"Yes".equalsIgnoreCase(record.get(ACTIVE))) {
                    log.warn("skipping non-active row {} : {}", record.getRecordNumber(), record.get(ORG_NAME));
                } else {
                    entities.add(toOrganisation(record));
                }
            } catch (RuntimeException rte) {
                summary.addError(record.getRecordNumber(), rte.getMessage());
                log.error("row {} does could not be loaded: {}", record.getRecordNumber(), rte.getMessage());
            }
        }

        summary.setEntitiesLoaded(entities.size());
        summary.setAllLoaded(entities.size() == records.size());

        log.info("{} organisations loaded", entities.size());

        return entities;
    }

    private OrganisationEntity toOrganisation(CSVRecord record) {
        OrganisationEntity organisation = new OrganisationEntity();
        if (StringUtils.isNotEmpty(record.get(ID))) {
            organisation.setId(Integer.parseInt(record.get(ID)));
        }
        organisation.setName(record.get(ORG_NAME));
        organisation.setProviderNumber(record.get(PROVIDER_NO));
        organisation.setEmail(record.get(EMAIL));
        organisation.setCeoTitle(record.get(CEO_TITLE));
        organisation.setCeoName(record.get(CEO_NAME));
        organisation.setEntityType(getEntityType(record, ENTITY_TYPE));
        organisation.setRegulated("Y".equalsIgnoreCase(record.get(REGULATED)));
        organisation.setViability(getScore(record, VIABILITY_SCORE));
        organisation.setGovernance(getScore(record, GOV_SCORE));
        organisation.setAddress(toAddress(record));

        if (organisation.getCeoTitle() != null && organisation.getCeoTitle().length() > 10) {
            organisation.setCeoTitle("?");
        }

        organisation.setManagingOrganisation(new OrganisationEntity(Organisation.GLA_HNL_ORG_ID, ""));
        organisation.setStatus(OrganisationStatus.Approved);

        return organisation;
    }

    private Address toAddress(CSVRecord record) {
        Address address = new Address();
        address.setAddress1(getString(record, ADDRESS_1, 255));
        address.setAddress2(record.get(ADDRESS_2));
        address.setAddress3(record.get(ADDRESS_3));
        address.setAddress4(record.get(ADDRESS_4));
        address.setAddress5(record.get(ADDRESS_5));
        address.setPostcode(record.get(POSTCODE));
        return address;
    }

    private String getString(CSVRecord record, String column, int maxLength) {
        String value = record.get(column);
        if (value == null) {
            return "";
        }
        if (value.length() > maxLength) {
            value = value.substring(0, maxLength);
        }
        return value;
    }

    private Integer getEntityType(CSVRecord record, String column) {
        String rawValue = record.get(column);

        if ("RP".equalsIgnoreCase(rawValue)) {
            return OrganisationType.PROVIDER.getId();
        } else if ("Registered Provider".equalsIgnoreCase(rawValue)) {
            return OrganisationType.LOCAL_AUTHORITY.getId();
        } else if ("Borough".equalsIgnoreCase(rawValue)) {
            return OrganisationType.LOCAL_AUTHORITY.getId();
        } else if ("Wholly owned by Borough".equalsIgnoreCase(rawValue)) {
            return OrganisationType.LOCAL_AUTHORITY.getId();
        } else {
            return OrganisationType.OTHER.getId();
        }
    }

    private String getScore(CSVRecord record, String column) {
        String score = record.get(column);
        if (score == null || score.length() != 2 || ! (score.startsWith("V") || score.startsWith("G"))) {
            return null;
        } else {
            return score;
        }
    }

}
