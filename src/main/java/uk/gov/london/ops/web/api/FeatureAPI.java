/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.web.api;

import io.swagger.annotations.Api;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import uk.gov.london.ops.FeatureStatus;
import uk.gov.london.ops.domain.user.Role;

@RestController
@RequestMapping("/api/v1")
@Api(description="feature toggle api")
public class FeatureAPI {

    @Autowired
    FeatureStatus featureStatus;

    @RequestMapping(value = "/features", method = RequestMethod.GET)
    public FeatureStatus.Feature[] getFeatures() {
        return FeatureStatus.Feature.values();
    }

    @RequestMapping(value = "/features/{feature}", method = RequestMethod.GET)
    public boolean isEnabled(@PathVariable String feature) {
        return featureStatus.isEnabled(toFeatureEnum(feature));
    }

    @Secured(Role.OPS_ADMIN)
    @RequestMapping(value = "/features/{feature}", method = RequestMethod.POST)
    public void setEnabled(@PathVariable String feature, @RequestBody String enabled) {
        featureStatus.setEnabled(toFeatureEnum(feature), Boolean.parseBoolean(enabled));
    }

    private FeatureStatus.Feature toFeatureEnum(String feature) {
        return FeatureStatus.Feature.valueOf(StringUtils.capitalize(feature));
    }

}
