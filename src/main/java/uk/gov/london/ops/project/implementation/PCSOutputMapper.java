/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.implementation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.london.common.CSVFile;
import uk.gov.london.common.CSVRowSource;
import uk.gov.london.ops.domain.project.OutputTableEntry;
import uk.gov.london.ops.service.project.ProjectOutputsService;
import uk.gov.london.ops.framework.MapResult;

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
