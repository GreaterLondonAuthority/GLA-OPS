/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.framework.feature;

import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;
import uk.gov.london.ops.audit.AuditService;
import uk.gov.london.ops.framework.environment.Environment;
import uk.gov.london.ops.framework.exception.ForbiddenAccessException;
import uk.gov.london.ops.user.UserService;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static uk.gov.london.ops.framework.OPSUtils.currentUsername;

/**
 * Status of feature toggles.
 *
 * @author Steve Leach
 */
@Component
public class FeatureStatusImpl implements FeatureStatus, InfoContributor {

    final AuditService auditService;
    final UserService userService;
    final Environment environment;
    final FeatureRepository featureRepository;

    public FeatureStatusImpl(AuditService auditService, UserService userService, Environment environment,
                             FeatureRepository featureRepository) {
        this.auditService = auditService;
        this.userService = userService;
        this.environment = environment;
        this.featureRepository = featureRepository;
    }

    public boolean isEnabled(Feature feature) {
        return featureRepository.isFeatureEnabled(feature.name());
    }

    public boolean isEnabled(String featureName) {
        if (!featureRepository.isFeatureEnabled(featureName)) {
            throw new ForbiddenAccessException(featureName + " feature is currently disabled.");
        } else {
            return true;
        }
    }

    public FeatureEntity setEnabled(Feature feature, boolean enabled, String auditUsername, String auditMessage) {
        FeatureEntity featureEntity = setEnabled(feature, enabled);
        if (featureEntity != null) {
            auditService.auditActivityForUser(auditUsername, auditMessage);
        }
        return featureEntity;
    }

    public FeatureEntity setEnabled(Feature feature, boolean enabled) {
        if (featureRepository.existsById(feature.name())) {
            FeatureEntity featureEntity = featureRepository.getOne(feature.name());
            featureEntity.setModifiedBy(currentUsername());
            featureEntity.setModifiedOn(environment.now());
            featureEntity.setEnabled(enabled);
            featureRepository.save(featureEntity);
            return enrich(featureEntity);
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
        List<FeatureEntity> features = featureRepository.findAll();
        for (FeatureEntity feature: features) {
            enrich(feature);
        }
        return features;
    }

    private FeatureEntity enrich(FeatureEntity feature) {
        if (feature.getModifiedBy() != null) {
            feature.setModifiedByName(userService.getUserFullName(feature.getModifiedBy()));
        }
        return feature;
    }

    public void contribute(Info.Builder builder) {
        Map<String, Object> data = new TreeMap<>();

        for (Feature feature: Feature.values()) {
            data.put(feature.name(), isEnabled(feature));
        }

        builder.withDetail("featureToggles", data);
    }

}
