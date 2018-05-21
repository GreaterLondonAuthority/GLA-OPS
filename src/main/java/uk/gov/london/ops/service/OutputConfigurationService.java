/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.london.ops.domain.outputs.OutputCategoryConfiguration;
import uk.gov.london.ops.domain.outputs.OutputConfigurationGroup;
import uk.gov.london.ops.domain.outputs.OutputType;
import uk.gov.london.ops.exception.ValidationException;
import uk.gov.london.ops.repository.OutputConfigurationGroupRepository;
import uk.gov.london.ops.repository.OutputTypeRepository;
import uk.gov.london.ops.repository.ProjectOutputConfigurationRepository;

import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional
public class OutputConfigurationService  {


    @Autowired
    private OutputConfigurationGroupRepository outputConfigurationGroupRepository;

    @Autowired
    private ProjectOutputConfigurationRepository projectOutputConfigurationRepository;

    @Autowired
    private OutputTypeRepository outputTypeRepository;


    public List<OutputType> getAllOutputTypes() {
        return outputTypeRepository.findAll();
    }
    public OutputType createOutputType(OutputType outputType) {
        if (outputType.getKey() == null) {
            throw new ValidationException("Unable to create output type without a Key being specified");
        }

        if (outputTypeRepository.findOne(outputType.getKey()) != null) {
            throw new ValidationException(String.format("Unable to create output type as item with same Key: %s already exists", outputType.getKey()));
        }

        if (StringUtils.isEmpty(outputType.getDescription())) {
            throw new ValidationException("Unable to create output type as output type is mandatory");
        }

        return outputTypeRepository.save(outputType);
    }

    public List<OutputCategoryConfiguration> getAllOutputCategories() {
        return projectOutputConfigurationRepository.findAll();
    }
    public OutputCategoryConfiguration createOutputCategory(OutputCategoryConfiguration categoryConfiguration) {
        if (categoryConfiguration.getId() == null) {
            throw new ValidationException("Unable to create category without an id being specified");
        }

        if (projectOutputConfigurationRepository.findOne(categoryConfiguration.getId()) != null) {
            throw new ValidationException(String.format("Unable to create output category as item with same ID: %d already exists", categoryConfiguration.getId()));
        }

        if (StringUtils.isEmpty(categoryConfiguration.getCategory()) || StringUtils.isEmpty(categoryConfiguration.getSubcategory()) || categoryConfiguration.getValueType() == null) {
            throw new ValidationException("Unable to create output category: category, subcategory and value type are mandatory");
        }

        return projectOutputConfigurationRepository.save(categoryConfiguration);
    }

    public OutputConfigurationGroup createOutputConfigurationGroup(OutputConfigurationGroup group) {
        if (group.getId() == null) {
            throw new ValidationException("Unable to create output group without an id being specified");
        }

        if (outputConfigurationGroupRepository.findOne(group.getId()) != null) {
            throw new ValidationException(String.format("Unable to create output group as group with same ID: %d already exists", group.getId()));

        }

        if (group.getCategoryIDs() != null) {
            group.getCategories().clear();

            for (Integer id : group.getCategoryIDs()) {
                OutputCategoryConfiguration cat = projectOutputConfigurationRepository.findOne(id);
                if (cat == null) {
                    throw new ValidationException(String.format("Unable to find output category with ID: %d, please create it first", id));
                }
                group.getCategories().add(cat);
            }
        }
        if (group.getOutputTypeKeys() != null) {
            group.getOutputTypes().clear();

            for (String key : group.getOutputTypeKeys()) {
                OutputType outputType = outputTypeRepository.findOne(key);
                if (outputType == null) {
                    throw new ValidationException(String.format("Unable to find output type with Key: %s, please create it first", key));
                }
                group.getOutputTypes().add(outputType);
            }
        }

        return outputConfigurationGroupRepository.save(group);
    }

    public OutputConfigurationGroup getGroup(Integer groupId) {
        return outputConfigurationGroupRepository.findOne(groupId);
    }

    public OutputType findOutputTypeByKey(String key) {
        return outputTypeRepository.findOne(key);
    }


}
