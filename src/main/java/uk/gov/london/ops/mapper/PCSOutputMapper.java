/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.london.ops.domain.project.OutputTableEntry;
import uk.gov.london.ops.mapper.model.MapResult;
import uk.gov.london.ops.service.project.ProjectOutputsService;
import uk.gov.london.ops.util.CSVFile;
import uk.gov.london.ops.util.CSVRowSource;

@Component
public class PCSOutputMapper implements CSVFile.CSVMapper<MapResult<OutputTableEntry>> {

    @Autowired
    ProjectOutputsService projectOutputsService;


    @Override
    @SuppressWarnings("unchecked")
    public MapResult<OutputTableEntry> mapRow(CSVRowSource csv) {
        return projectOutputsService.loadCSVRow(csv);

    }




}
