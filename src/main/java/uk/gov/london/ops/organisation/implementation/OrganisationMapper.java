/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.organisation.implementation;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import uk.gov.london.common.organisation.OrganisationType;
import uk.gov.london.ops.domain.organisation.Address;
import uk.gov.london.ops.domain.organisation.Organisation;
import uk.gov.london.ops.domain.organisation.OrganisationStatus;
import uk.gov.london.ops.web.model.BulkUploadSummary;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

@Component
public class OrganisationMapper {

    Logger log = LoggerFactory.getLogger(getClass());

    private static final String AREA_LEAD       = "Area Lead";
    private static final String TEAM            = "Team";
    private static final String ORG_NAME        = "Org.Name";
    private static final String ACTIVE          = "Active";
    private static final String IMS_NUM         = "IMS No.";
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
            AREA_LEAD, TEAM, ORG_NAME, ACTIVE, IMS_NUM, GAPS, SAP_NUM, ENTITY_TYPE, EMAIL,
            ADDRESS_1, ADDRESS_2, ADDRESS_3, ADDRESS_4, ADDRESS_5, POSTCODE,
            CEO_TITLE, CEO_NAME, REGULATED, VIABILITY_SCORE, GOV_SCORE, ID, STATUS
    };


    public List<Organisation> toEntities(InputStream csvInputStream, BulkUploadSummary summary) throws IOException {
        List<CSVRecord> records = CSVFormat.DEFAULT.withNullString("").withHeader(HEADERS).withSkipHeaderRecord(true)
                .withTrim().parse(new InputStreamReader(csvInputStream, UTF_8)).getRecords();

        if (summary != null) {
            summary.setSourceRows(records.size());
        }

        List<Organisation> entities = new ArrayList<>();

        for (CSVRecord record: records) {
            try {
                if (StringUtils.isEmpty(record.get(ORG_NAME))) {
                    log.warn("skipping row {} without organisation name", record.getRecordNumber());
                    continue;
                }

                if (! "Yes".equalsIgnoreCase(record.get(ACTIVE))) {
                    log.warn("skipping non-active row {} : {}", record.getRecordNumber(), record.get(ORG_NAME));
                    continue;
                }


                Address address = new Address();
                address.setAddress1(getString(record,ADDRESS_1,255));
                address.setAddress2(record.get(ADDRESS_2));
                address.setAddress3(record.get(ADDRESS_3));
                address.setAddress4(record.get(ADDRESS_4));
                address.setAddress5(record.get(ADDRESS_5));
                address.setPostcode(record.get(POSTCODE));

                Organisation organisation = new Organisation();
                if (StringUtils.isNotEmpty(record.get(ID))) {
                    organisation.setId(Integer.parseInt(record.get(ID)));
                }
                organisation.setName(record.get(ORG_NAME));
                organisation.setImsNumber(record.get(IMS_NUM));
                organisation.setEmail(record.get(EMAIL));
                organisation.setCeoTitle(record.get(CEO_TITLE));
                organisation.setCeoName(record.get(CEO_NAME));
                organisation.setEntityType(getEntityType(record,ENTITY_TYPE));
                organisation.setRegulated("Y".equalsIgnoreCase(record.get(REGULATED)));
                organisation.setViability(getScore(record,VIABILITY_SCORE));
                organisation.setGovernance(getScore(record,GOV_SCORE));
                organisation.setAddress(address);

                if (organisation.getCeoTitle() != null && organisation.getCeoTitle().length() > 10) {
                    organisation.setCeoTitle("?");
                }

                organisation.setManagingOrganisation(new Organisation(Organisation.GLA_HNL_ID, ""));
                organisation.setStatus(OrganisationStatus.Approved);

                entities.add(organisation);
            } catch (RuntimeException rte) {
                rte.printStackTrace();
                if (summary != null) {
                    summary.addError(record.getRecordNumber(), rte.getMessage());
                }
                log.error("row {} does could not be loaded: {}", record.getRecordNumber(), rte.getMessage());
                continue;
            }
        }

        if (summary != null) {
            summary.setEntitiesLoaded(entities.size());
            summary.setAllLoaded(entities.size() == records.size());
        }

        log.info("{} organisations loaded", entities.size());

        return entities;
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
            return OrganisationType.PROVIDER.id();
        } else if ("Registered Provider".equalsIgnoreCase(rawValue)) {
            return OrganisationType.BOROUGH.id();
        } else if ("Borough".equalsIgnoreCase(rawValue)) {
            return OrganisationType.BOROUGH.id();
        } else if ("Wholly owned by Borough".equalsIgnoreCase(rawValue)) {
            return OrganisationType.BOROUGH.id();
        } else {
            return OrganisationType.OTHER.id();
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
