/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.refdata;

import java.util.List;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.london.common.GlaUtils;
import uk.gov.london.ops.framework.exception.NotFoundException;
import uk.gov.london.ops.framework.exception.ValidationException;
import uk.gov.london.ops.project.ProjectFacade;
import uk.gov.london.ops.refdata.implementation.repository.OutputCategoryConfigurationRepository;
import uk.gov.london.ops.refdata.implementation.repository.OutputCategoryConfigurationSummaryRepository;
import uk.gov.london.ops.refdata.implementation.repository.OutputConfigurationGroupRepository;
import uk.gov.london.ops.refdata.implementation.repository.OutputTypeRepository;

@Service
@Transactional
public class OutputConfigurationService {

    @Autowired
    private OutputConfigurationGroupRepository outputConfigurationGroupRepository;

    @Autowired
    private OutputCategoryConfigurationRepository outputCategoryConfigurationRepository;

    @Autowired
    private OutputCategoryConfigurationSummaryRepository outputCategoryConfigurationSummaryRepository;

    @Autowired
    private ProjectFacade projectFacade;

    @Autowired
    private RefDataService refDataService;

    @Autowired
    private OutputTypeRepository outputTypeRepository;

    public List<OutputType> getAllOutputTypes() {
        return outputTypeRepository.findAll();
    }

    public OutputType getOutputType(String key) {
        return outputTypeRepository.getOne(key);
    }

    public OutputType createOutputType(OutputType outputType) {
        if (outputType.getKey() == null) {
            throw new ValidationException("Unable to create output type without a Key being specified");
        }

        if (outputTypeRepository.existsById(outputType.getKey())) {
            throw new ValidationException(
                    String.format("Unable to create output type as item with same Key: %s already exists", outputType.getKey()));
        }

        if (StringUtils.isEmpty(outputType.getDescription())) {
            throw new ValidationException("Unable to create output type as output type is mandatory");
        }

        return outputTypeRepository.save(outputType);
    }

    public List<OutputCategoryConfigurationSummary> getAllOutputCategories(String categoryName) {
        List<OutputCategoryConfigurationSummary> all = outputCategoryConfigurationSummaryRepository.findAll();
        if(categoryName != null) {
            return all.stream().filter(category -> category.getCategory().toLowerCase().contains(categoryName.toLowerCase())).collect(Collectors.toList());
        } else {
            return all;
        }
    }

    public OutputCategoryConfiguration getOutputCategory(Integer categoryId) {
        return outputCategoryConfigurationRepository.findById(categoryId).orElse(null);
    }

    public OutputCategoryConfiguration createOutputCategory(OutputCategoryConfiguration categoryConfiguration) {
        validateOutputCategoryConfigurationForCreate(categoryConfiguration);

        if (outputCategoryConfigurationRepository.existsById(categoryConfiguration.getId())) {
            throw new ValidationException(
                    String.format("Unable to create output category as item with same ID: %d already exists",
                            categoryConfiguration.getId()));
        }

        return save(categoryConfiguration);
    }

    public OutputCategoryConfiguration save(OutputCategoryConfiguration categoryConfiguration) {
        return outputCategoryConfigurationRepository.save(categoryConfiguration);
    }

    public OutputConfigurationGroup save(OutputConfigurationGroup group) {
        return outputConfigurationGroupRepository.save(group);
    }

    public OutputCategoryConfiguration createOutputCategoryConfiguration(Integer groupId,
                                                                          OutputCategoryConfiguration categoryConfiguration) {
        if (groupId != null && this.findGroup(groupId) == null) {
            throw new ValidationException("Unable to find group with ID: " + groupId);
        }

        Integer tenureTypeId = categoryConfiguration.getTenureTypeId();
        if (tenureTypeId != null && refDataService.getTenureType(tenureTypeId) == null) {
            throw new ValidationException("Unable to find Tenure Type with ID: " + tenureTypeId);
        }

        OutputCategoryConfiguration outputCategory = this.createOutputCategory(categoryConfiguration);
        if (groupId != null) {
            this.addCategoryToOutputGroup(groupId, outputCategory.getId());
        }

        return outputCategory;
    }

    public void updateOutputCategory(OutputCategoryConfiguration categoryConfiguration) {

        OutputCategoryConfiguration fromDB = outputCategoryConfigurationRepository.getOne(categoryConfiguration.getId());

        validateOutputCategoryConfigurationForSave(fromDB, categoryConfiguration);

        outputCategoryConfigurationRepository.save(categoryConfiguration);
    }

    private void validateOutputCategoryConfigurationForCreate(OutputCategoryConfiguration categoryConfiguration) {
        if (categoryConfiguration.getId() == null) {
            throw new ValidationException("Unable to create category without an id being specified");
        }

        if (StringUtils.isEmpty(categoryConfiguration.getCategory()) || StringUtils
                .isEmpty(categoryConfiguration.getSubcategory()) || categoryConfiguration.getValueType() == null) {
            throw new ValidationException("Unable to create output category: category, subcategory and value type are mandatory");
        }
    }

    private void validateOutputCategoryConfigurationForSave(OutputCategoryConfiguration existing,
            OutputCategoryConfiguration categoryConfiguration) {

        this.validateOutputCategoryConfigurationForCreate(categoryConfiguration);

        if (!existing.getCategory().equals(categoryConfiguration.getCategory())) {
            throw new ValidationException("Unable to update an output category");
        }
    }

    public OutputConfigurationGroup createOutputConfigurationGroup(OutputConfigurationGroup group) {
        if (group.getId() == null) {
            throw new ValidationException("Unable to create output group without an id being specified");
        }
        if (outputConfigurationGroupRepository.existsById(group.getId())) {
            throw new ValidationException(
                    String.format("Unable to create output group as group with same ID: %d already exists", group.getId()));
        }
        if (group.getCategoryIDs() != null) {
            group.getCategories().clear();
            for (Integer id : group.getCategoryIDs()) {
                OutputCategoryConfiguration cat = outputCategoryConfigurationRepository.findById(id).orElseThrow(() ->
                        new ValidationException(
                                String.format("Unable to find output category with ID: %d, please create it first", id)));
                group.getCategories().add(cat);
            }
        }
        if (group.getOutputTypeKeys() != null) {
            group.getOutputTypes().clear();
            for (String key : group.getOutputTypeKeys()) {
                OutputType outputType = outputTypeRepository.findById(key).orElseThrow(() -> new ValidationException(
                        String.format("Unable to find output type with Key: %s, please create it first", key)));
                group.getOutputTypes().add(outputType);
            }
        }
        return save(group);
    }

    public OutputConfigurationGroup getGroup(Integer groupId) {
        OutputConfigurationGroup outputConfigGroup = findGroup(groupId);
        if (outputConfigGroup == null) {
            throw new NotFoundException();
        }
        return outputConfigGroup;
    }

    public OutputConfigurationGroup findGroup(Integer groupId) {
        OutputConfigurationGroup outputConfigGroup = outputConfigurationGroupRepository.findById(groupId).orElse(null);
        return outputConfigGroup;
    }

    public OutputType findOutputTypeByKey(String key) {
        return outputTypeRepository.findById(key).orElse(null);
    }


    public OutputCategoryConfiguration hideOutputCategory(Integer categoryId, Boolean hide) {
        OutputCategoryConfiguration category = outputCategoryConfigurationRepository.findById(categoryId).orElse(null);
        if (category == null) {
            throw new NotFoundException();
        }

        category.setHidden(hide);
        return outputCategoryConfigurationRepository.save(category);
    }


    public OutputConfigurationGroup addCategoryToOutputGroup(Integer groupId, Integer categoryId) {
        if (groupId == null || categoryId == null) {
            throw new ValidationException(
                    "Unable to add category to output group without group and category ids being specified");
        }

        OutputConfigurationGroup group = outputConfigurationGroupRepository.findById(groupId).orElse(null);

        if (group == null) {
            throw new ValidationException(String.format("Unable to find output group as group with ID: %d", groupId));
        }

        if (group.getCategories().stream().anyMatch(e -> e.getId().equals(categoryId))) {
            throw new ValidationException(String.format("Category with ID: %d already exists in a group", categoryId));
        }

        OutputCategoryConfiguration cat = outputCategoryConfigurationRepository.findById(categoryId).orElse(null);
        if (cat == null) {
            throw new ValidationException(
                    String.format("Unable to find output category with ID: %d, please create it first", categoryId));
        }

        group.getCategories().add(cat);
        return outputConfigurationGroupRepository.save(group);
    }

    public void updateOutputCategoryName(Integer id, String oldName, String newName) {
        OutputConfigurationGroup one = outputConfigurationGroupRepository.getOne(id);
        List<OutputCategoryConfiguration> categories = one.getCategories();
        for (OutputCategoryConfiguration category : categories) {
            if (category.getCategory().equals(oldName)) {
                category.setCategory(newName);
                outputCategoryConfigurationRepository.save(category);
            }
        }

        projectFacade.updateAssumptionsAffectedByCategoryChange(id, oldName, newName);
    }

    public List<OutputConfigurationGroup> getAllOutputConfigurationGroup() {
        return outputConfigurationGroupRepository.findAll();
    }

    public OutputCategoryConfiguration findByCategoryAndSubcategory(String category, String subCategory) {
        return outputCategoryConfigurationRepository
                .findByCategoryAndSubcategory(GlaUtils.superTrim(category), GlaUtils.superTrim(subCategory));
    }

}
