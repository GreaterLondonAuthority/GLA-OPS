/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.framework.feature;

import io.swagger.annotations.Api;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import uk.gov.london.ops.framework.annotations.PermissionRequired;

import java.util.List;

import static uk.gov.london.common.user.BaseRole.OPS_ADMIN;
import static uk.gov.london.ops.service.PermissionType.SYS_FEATURES_EDIT;

@RestController
@RequestMapping("/api/v1")
@Api(description="feature toggle api")
public class FeatureAPI {

    @Autowired
    FeatureStatus featureStatus;

    @RequestMapping(value = "/features", method = RequestMethod.GET)
    public List<FeatureEntity> getFeatures() {
        return featureStatus.findAll();
    }

    @RequestMapping(value = "/features/{feature}", method = RequestMethod.GET)
    public boolean isEnabled(@PathVariable String feature) {
        return featureStatus.isEnabled(toFeatureEnum(feature));
    }

    @PermissionRequired(SYS_FEATURES_EDIT)
    @RequestMapping(value = "/features/{feature}", method = RequestMethod.POST)
    public FeatureEntity setEnabled(@PathVariable String feature, @RequestBody String enabled) {
        return featureStatus.setEnabled(toFeatureEnum(feature), Boolean.parseBoolean(enabled));
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/features/{feature}", method = RequestMethod.DELETE)
    public void delete(@PathVariable String feature) {
        featureStatus.delete(toFeatureEnum(feature));
    }

    private Feature toFeatureEnum(String feature) {
        return Feature.valueOf(StringUtils.capitalize(feature));
    }

}
