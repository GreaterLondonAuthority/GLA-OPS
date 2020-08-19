/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.implementation.spe;

import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;

/**
 * Created by chris on 28/02/2017.
 */
@Component
public class SimpleProjectExportConfig {

    @Resource(name = "mappingProperties")
    private Map<String, String> mappingProperties;

    public String getReplacementProperty(String key) {
        return mappingProperties.get(key);
    }

}
