/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.report;

import org.springframework.stereotype.Component;
import uk.gov.london.ops.domain.report.BoroughReportItem;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.persistence.Column;
import java.lang.reflect.Field;
import java.util.*;

/**
 * Created by chris on 20/09/2017.
 */
@Component
public class BoroughReportProfile extends BaseReportProfile {

    private Set<String> headers;


    @Resource(name = "boroughReportMappingProperties")
    private Map<String, String> headerMapper;


    private Map<String, Field> boroughReportFieldHeaderMapper;

    /**
     * This set up the metadata for Borough report: Header names
     * (headers) and field metadata
     * (boroughReportFieldHeaderMapper)
     *
     * Notes:
     * - headers contains a sorted list of header names. I is used
     * to generate the CSV file. This just order the map headerMapper,
     * which provides the header name for every column.
     *
     * - boroughReportFieldHeaderMapper contains a map with the fields of the
     * BoroughReportItem entity, indexed by header name. This will helps to
     * retrieves the field's value dynamically based on the header. This is done
     * by using headerMapper, which is indexed by column name in the
     * database and the
     *
     * @column annotation in the BoroughReportItem.
     */
    @PostConstruct
    private void boroughReportMetadataSetUp() {
        //LinkedHashSet is used to keep the inserting order. DON'T change it
        headers = new LinkedHashSet<>();
        headers.add(headerMapper.get("programme_id"));
        headers.add(headerMapper.get("programme_name"));
        headers.add(headerMapper.get("project_type"));
        headers.add(headerMapper.get("project_id"));
        headers.add(headerMapper.get("project_title"));
        headers.add(headerMapper.get("date_submitted"));
        headers.add(headerMapper.get("description"));
        headers.add(headerMapper.get("org_type"));
        headers.add(headerMapper.get("lead_org_name"));
        headers.add(headerMapper.get("developing_org"));
        headers.add(headerMapper.get("borough"));
        headers.add(headerMapper.get("postcode"));
        headers.add(headerMapper.get("x_coord"));
        headers.add(headerMapper.get("y_coord"));
        headers.add(headerMapper.get("ms_processing_route"));
        headers.add(headerMapper.get("planning_ref"));
        headers.add(headerMapper.get("ms_start_site"));
        headers.add(headerMapper.get("ms_completion"));
        headers.add(headerMapper.get("affordable_criteria_met_dev_led"));
        headers.add(headerMapper.get("s106_dev_led"));
        headers.add(headerMapper.get("add_aff_units_dev_led"));
        headers.add(headerMapper.get("lar_units"));
        headers.add(headerMapper.get("llr_units"));
        headers.add(headerMapper.get("lso_units"));
        headers.add(headerMapper.get("other_units"));
        headers.add(headerMapper.get("q_other_aff_type"));
        headers.add(headerMapper.get("q_planning_status"));
        headers.add(headerMapper.get("q_land_status"));
        headers.add(headerMapper.get("q_larger_aff_homes"));
        headers.add(headerMapper.get("eg_supp_units"));
        headers.add(headerMapper.get("aq_wheelchair_units"));
        headers.add(headerMapper.get("aq_client_group"));

        //Adding any other header specified in properties,
        // but not added in headers
        headerMapper.entrySet()
                .stream()
                .map(Map.Entry::getValue)
                .filter(v -> !headers.contains(v))
                .forEach(headers::add);

        boroughReportFieldHeaderMapper = new HashMap<>();
        for(final Field field : BoroughReportItem.class.getDeclaredFields()) {
            field.setAccessible(true);
            if(field.isAnnotationPresent(Column.class)) {
                final String columnName = field.getAnnotation(Column.class)
                        .name();
                final String headerName = headerMapper.get(columnName);
                if(headerName != null) {
                    boroughReportFieldHeaderMapper.put(headerName, field);
                }
            }
        }
    }


    /**
     * It gets a list of BoroughReportItem objects and convert them to Map .
     * It uses the boroughReportFieldHeaderMapper where we have the Class field
     * indexed by header name
     *
     * @param list List of BoroughReportItems to be converted to Map objects
     * @return Map with the field, indexed by header name
     */
    public List<Map<String, Object>> convertBoroughReportItemsToMap(
            final List<BoroughReportItem> list)
            throws IllegalAccessException {
        final List<Map<String, Object>> result = new ArrayList<>();
        if(list != null) {

            for(BoroughReportItem item : list) {
                final Map<String, Object> line = new HashMap<>();
                for(Map.Entry<String, Field> entry :
                        boroughReportFieldHeaderMapper.entrySet()) {
                    Object obj = getReportFieldValue(entry.getValue().get(item));
                    line.put(entry.getKey(), obj);
                }
                result.add(line);
            }
        }
        return result;
    }




    public Set<String> getHeaders() {
        return headers;
    }
}
