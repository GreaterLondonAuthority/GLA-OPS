/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.repeatingentity;

import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/userDefinedOutput")
@Api("managing project user defined output data")
public class ProjectUserDefinedOutputAPI extends RepeatingEntityAPI<UserDefinedOutput> {

    @Autowired
    public ProjectUserDefinedOutputAPI(ProjectUserDefinedOutputService service) {
        super(service);
    }

}
