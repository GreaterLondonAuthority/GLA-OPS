/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.framework.feature;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;
import uk.gov.london.ops.Environment;
import uk.gov.london.ops.audit.AuditService;
import uk.gov.london.ops.service.UserService;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Status of feature toggles.
 *
 * @author Steve Leach
 */
@Component
public class FeatureStatus implements InfoContributor {

    Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    AuditService auditService;

    @Autowired
    UserService userService;

    @Autowired
    Environment environment;

    @Autowired
    FeatureRepository featureRepository;

    public boolean isEnabled(Feature feature) {
        return featureRepository.isFeatureEnabled(feature.name());
    }

    public FeatureEntity setEnabled(Feature feature, boolean enabled) {
        if (featureRepository.existsById(feature.name())) {
            FeatureEntity featureEntity = featureRepository.getOne(feature.name());
            featureEntity.setModifiedBy(userService.currentUser());
            featureEntity.setModifiedOn(environment.now());
            featureEntity.setEnabled(enabled);
            featureRepository.save(featureEntity);
            return featureEntity;
        }
        return null;
    }

    public void delete(Feature feature) {
        if (featureRepository.existsById(feature.name())) {
            auditService.auditCurrentUserActivity("Feature was removed: " + feature.name());
            featureRepository.deleteById(feature.name());
        }
    }

    public List<FeatureEntity> findAll() {
        return featureRepository.findAll();
    }

    public void contribute(Info.Builder builder) {
        Map<String,Object> data = new TreeMap<>();

        for (Feature feature: Feature.values()) {
            data.put(feature.name(), isEnabled(feature));
        }

        builder.withDetail("featureToggles", data);
    }

}
