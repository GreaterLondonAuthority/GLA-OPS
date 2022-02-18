/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.organisation.template;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;
import uk.gov.london.ops.organisation.OrganisationType;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service endpoint for Organisation Template data.
 *
 * Created by cmatias on 12/10/2020.
 */
@Transactional
@Service
public class OrganisationTemplateService {

    Logger log = LoggerFactory.getLogger(getClass());

    private final Map<String, OrganisationTemplate> organisationTemplates = new HashMap<>();

    @PostConstruct
    public void postConstruct() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            PathMatchingResourcePatternResolver resourceLoader = new PathMatchingResourcePatternResolver();
            Resource[] resources = resourceLoader.getResources("classpath:uk/gov/london/ops/organisation/template/*.json");
            for (Resource resource : resources) {
                InputStream resourceInputStream = resource.getInputStream();
                OrganisationTemplate template = mapper.readValue(resourceInputStream, OrganisationTemplate.class);
                organisationTemplates.put(template.getName(), template);
            }
        } catch (IOException e) {
            log.error("Organisation templates could not load. Due to: " + e.getMessage());
        }
    }

    /**
     * Returns a map of all org types for an organisation. The map preserves the display order set on organisation template.
     */
    public LinkedHashMap<String, String> getLegalStatuses() {
        LinkedHashMap<String, String> result = new LinkedHashMap<>();
        for (OrganisationTemplate entry : getOrganisationTemplates()) {
            result.put(entry.getName(), entry.getDescription());
        }
        return result;
    }

    /**
     * Returns list of all organisation templates for each org type.
     */
    public List<OrganisationTemplate> getOrganisationTemplates() {
        return organisationTemplates.values().stream()
                .sorted(Comparator.comparing(OrganisationTemplate::getDisplayOrder))
                .collect(Collectors.toList());
    }

    public OrganisationTemplate getOrganisationTemplate(OrganisationType organisationType) {
        List<OrganisationTemplate> orgTemplates = getOrganisationTemplates();
        return orgTemplates.stream()
                .filter(ot -> ot.getName().equals(organisationType.name()))
                .findFirst()
                .orElse(new OrganisationTemplate());
    }
}
