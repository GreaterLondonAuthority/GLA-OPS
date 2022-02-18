/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.refdata.implementation.di;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.london.ops.framework.di.DataInitialiserModule;
import uk.gov.london.ops.refdata.OutputCategoryConfiguration;
import uk.gov.london.ops.refdata.OutputConfigurationGroup;
import uk.gov.london.ops.refdata.OutputType;
import uk.gov.london.ops.refdata.implementation.repository.OutputCategoryConfigurationRepository;
import uk.gov.london.ops.refdata.implementation.repository.OutputConfigurationGroupRepository;
import uk.gov.london.ops.refdata.implementation.repository.OutputTypeRepository;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static uk.gov.london.ops.refdata.TenureType.LONDON_AFFORDABLE_RENT;
import static uk.gov.london.ops.refdata.TenureType.LONDON_LIVING_RENT;

@Transactional
@Component
public class OutputCategoriesDataInitialiser implements DataInitialiserModule {

    @Autowired
    private OutputCategoryConfigurationRepository configurationRepository;

    @Autowired
    private OutputTypeRepository outputTypeRepository;

    @Autowired
    private OutputConfigurationGroupRepository outputConfigurationGroupRepository;

    // don't change this is used in r1.5 liquibase script
    private static final int DEFAULT_CONFIG_GROUP_ID = 1000;
    private static final int REGEN_CONFIG_GROUP_ID = 1001;
    private static final int SINGLE_OUTPUT_GROUP_ID = 1002;
    private static final int SKILLS_ESF_OUTPUT_GROUP_ID = 1003;
    private static final int GOOD_GROWTH_FUND_OUTPUT_GROUP_ID = 1007;

    public static final int HIDDEN_OUTPUT_CATEGORY_ID = 111;

    private static final String HOUSING_STARTS_ON_SITE_AFFORDABLE = "Housing Starts on Site (Affordable)";
    private static final String HOUSING_COMPLETIONS_AFFORDABLE = "Housing Completions (Affordable)";
    private static final String HOUSING_STARTS_ON_SITE_OPEN_MARKET = "Housing Starts on Site (Open Market)";
    private static final String HOUSING_COMPLETIONS_OPEN_MARKET = "Housing Completions (Open Market)";
    private static final String LAND_BROUGHT_INTO_BENEFICIAL_USE = "Land Brought into Beneficial Use (Ha)";
    private static final String EMPLOYMENT_FLOORSPACE = "Employment Floorspace (sqm)";
    private static final String APPRENTICESHIP_POSITIONS = "Apprenticeship Positions";
    private static final String JOBS_CREATED = "Jobs Created (Potential)";
    private static final String DEVELOPMENT_VALUE = "Development Value (£)";
    private static final String ADDITIONAL_LEARNERS_SUPPORTED = "Additional learners supported";
    private static final String COST_SAVINGS = "Cost savings";
    private static final String ADDITIONAL_JOBS_CREATED = "Additional jobs created";
    private static final String ADDITIONAL_APPRENTICESHIP_STARTS = "Additional apprenticeship starts";
    private static final String ADDITIONAL_SEND_LEARNERS_SUPPORTED = "Additional SEND learners supported";
    private static final String ADDITIONAL_REDUCTION_IN_NEETS = "Additional reduction in NEETs";
    private static final String ADDITIONAL_LEARNERS_THAT_WILL_NOT_BECOME_NEET = "Additional learners that will not become NEET";
    private static final String ADDITIONAL_STUDENTS_PROGRESSING_TO_EMPLOYMENT = "Additional students progressing to employment";

    private static final String SOCIAL_RENT = "Social Rent";
    private static final String INTERMEDIATE_RENT = "Intermediate Rent";
    private static final String SHARED_OWNERSHIP = "Shared Ownership";
    private static final String SHARED_EQUITY = "Shared Equity";
    private static final String AFFORDABLE_HOUSING_LEGACY = "Affordable Housing Legacy";
    private static final String LEGACY = "Legacy";
    private static final String PRIVATE_RENT = "Private Rent";
    private static final String PRIVATE_SALES = "Private Sales";

    @Override
    public String getName() {
        return "Outputs categories data initialiser";
    }

    @Override
    public void addReferenceData() {
        OutputType directOutputType = new OutputType("DIRECT", "Direct Output");

        List<OutputType> defaultOutputTypes = new ArrayList<>();
        defaultOutputTypes.add(directOutputType);
        defaultOutputTypes.add(new OutputType("IND_COUNTED_IN_ANOTHER", "Indirect: Counted in Another Housing Programme"));
        defaultOutputTypes.add(new OutputType("IND_MINORITY_STAKE", "Indirect: Minority Stake in Joint Venture"));
        defaultOutputTypes.add(new OutputType("IND_UNBLOCKS", "Indirect: Unlocks Other Parts of a Site"));
        defaultOutputTypes.add(new OutputType("IND_UNLOCKING", "Indirect: Unlocking Without Land Interest"));
        defaultOutputTypes.add(new OutputType("IND_OTHER", "Indirect: Other"));

        List<OutputType> singleOutputType = new ArrayList<>();
        singleOutputType.add(directOutputType);

        List<OutputType> regenOutputTypes = new ArrayList<>();
        regenOutputTypes.add(directOutputType);
        regenOutputTypes.add(new OutputType("INDIRECT", "Indirect Output"));

        List<OutputType> ggfOutputTypes = new ArrayList<>();
        ggfOutputTypes.add(new OutputType("DIRECT", "Direct Output"));
        ggfOutputTypes.add(new OutputType("INDIRECT", "Indirect Output"));

        List<OutputCategoryConfiguration> defaultOutputCategories = new ArrayList<>();
        defaultOutputCategories.add(new OutputCategoryConfiguration(1, HOUSING_STARTS_ON_SITE_AFFORDABLE, SOCIAL_RENT,
                OutputCategoryConfiguration.InputValueType.UNITS));
        defaultOutputCategories.add(new OutputCategoryConfiguration(2, HOUSING_STARTS_ON_SITE_AFFORDABLE, LONDON_AFFORDABLE_RENT,
                OutputCategoryConfiguration.InputValueType.UNITS));
        defaultOutputCategories.add(new OutputCategoryConfiguration(3, HOUSING_STARTS_ON_SITE_AFFORDABLE, INTERMEDIATE_RENT,
                OutputCategoryConfiguration.InputValueType.UNITS));
        defaultOutputCategories.add(new OutputCategoryConfiguration(4, HOUSING_STARTS_ON_SITE_AFFORDABLE, LONDON_LIVING_RENT,
                OutputCategoryConfiguration.InputValueType.UNITS));
        defaultOutputCategories.add(new OutputCategoryConfiguration(5, HOUSING_STARTS_ON_SITE_AFFORDABLE, SHARED_OWNERSHIP,
                OutputCategoryConfiguration.InputValueType.UNITS));
        defaultOutputCategories.add(new OutputCategoryConfiguration(6, HOUSING_STARTS_ON_SITE_AFFORDABLE, SHARED_EQUITY,
                OutputCategoryConfiguration.InputValueType.UNITS));
        defaultOutputCategories
                .add(new OutputCategoryConfiguration(7, HOUSING_STARTS_ON_SITE_AFFORDABLE, AFFORDABLE_HOUSING_LEGACY,
                        OutputCategoryConfiguration.InputValueType.UNITS));

        defaultOutputCategories.add(new OutputCategoryConfiguration(8, HOUSING_COMPLETIONS_AFFORDABLE, SOCIAL_RENT,
                OutputCategoryConfiguration.InputValueType.UNITS));
        defaultOutputCategories.add(new OutputCategoryConfiguration(9, HOUSING_COMPLETIONS_AFFORDABLE, LONDON_AFFORDABLE_RENT,
                OutputCategoryConfiguration.InputValueType.UNITS));
        defaultOutputCategories.add(new OutputCategoryConfiguration(10, HOUSING_COMPLETIONS_AFFORDABLE, INTERMEDIATE_RENT,
                OutputCategoryConfiguration.InputValueType.UNITS));
        defaultOutputCategories.add(new OutputCategoryConfiguration(11, HOUSING_COMPLETIONS_AFFORDABLE, LONDON_LIVING_RENT,
                OutputCategoryConfiguration.InputValueType.UNITS));
        defaultOutputCategories.add(new OutputCategoryConfiguration(12, HOUSING_COMPLETIONS_AFFORDABLE, SHARED_OWNERSHIP,
                OutputCategoryConfiguration.InputValueType.UNITS));
        defaultOutputCategories.add(new OutputCategoryConfiguration(13, HOUSING_COMPLETIONS_AFFORDABLE, SHARED_EQUITY,
                OutputCategoryConfiguration.InputValueType.UNITS));
        defaultOutputCategories.add(new OutputCategoryConfiguration(14, HOUSING_COMPLETIONS_AFFORDABLE, LEGACY,
                OutputCategoryConfiguration.InputValueType.UNITS));

        defaultOutputCategories.add(new OutputCategoryConfiguration(15, HOUSING_STARTS_ON_SITE_OPEN_MARKET, LEGACY,
                OutputCategoryConfiguration.InputValueType.UNITS));
        defaultOutputCategories.add(new OutputCategoryConfiguration(16, HOUSING_STARTS_ON_SITE_OPEN_MARKET, PRIVATE_RENT,
                OutputCategoryConfiguration.InputValueType.UNITS));
        defaultOutputCategories.add(new OutputCategoryConfiguration(17, HOUSING_STARTS_ON_SITE_OPEN_MARKET, PRIVATE_SALES,
                OutputCategoryConfiguration.InputValueType.UNITS));

        defaultOutputCategories.add(new OutputCategoryConfiguration(18, HOUSING_COMPLETIONS_OPEN_MARKET, LEGACY,
                OutputCategoryConfiguration.InputValueType.UNITS));
        defaultOutputCategories.add(new OutputCategoryConfiguration(19, HOUSING_COMPLETIONS_OPEN_MARKET, PRIVATE_RENT,
                OutputCategoryConfiguration.InputValueType.UNITS));
        defaultOutputCategories.add(new OutputCategoryConfiguration(20, HOUSING_COMPLETIONS_OPEN_MARKET, PRIVATE_SALES,
                OutputCategoryConfiguration.InputValueType.UNITS));

        defaultOutputCategories
                .add(new OutputCategoryConfiguration(21, LAND_BROUGHT_INTO_BENEFICIAL_USE, "Previously Developed/Brownfield",
                        OutputCategoryConfiguration.InputValueType.HECTARES));
        defaultOutputCategories.add(new OutputCategoryConfiguration(22, LAND_BROUGHT_INTO_BENEFICIAL_USE, "Greenfield",
                OutputCategoryConfiguration.InputValueType.HECTARES));
        defaultOutputCategories.add(new OutputCategoryConfiguration(23, LAND_BROUGHT_INTO_BENEFICIAL_USE, "Public Land",
                OutputCategoryConfiguration.InputValueType.HECTARES));

        defaultOutputCategories.add(new OutputCategoryConfiguration(24, APPRENTICESHIP_POSITIONS, "N/A",
                OutputCategoryConfiguration.InputValueType.POSITIONS));
        defaultOutputCategories.add(new OutputCategoryConfiguration(25, JOBS_CREATED, "N/A",
                OutputCategoryConfiguration.InputValueType.POSITIONS));

        defaultOutputCategories.add(new OutputCategoryConfiguration(26, EMPLOYMENT_FLOORSPACE, "A1",
                OutputCategoryConfiguration.InputValueType.SQUARE_METRES_NET));
        defaultOutputCategories.add(new OutputCategoryConfiguration(27, EMPLOYMENT_FLOORSPACE, "A2",
                OutputCategoryConfiguration.InputValueType.SQUARE_METRES_NET));
        defaultOutputCategories.add(new OutputCategoryConfiguration(28, EMPLOYMENT_FLOORSPACE, "A3",
                OutputCategoryConfiguration.InputValueType.SQUARE_METRES_NET));
        defaultOutputCategories.add(new OutputCategoryConfiguration(29, EMPLOYMENT_FLOORSPACE, "B1",
                OutputCategoryConfiguration.InputValueType.SQUARE_METRES_GROSS));
        defaultOutputCategories.add(new OutputCategoryConfiguration(30, EMPLOYMENT_FLOORSPACE, "B2",
                OutputCategoryConfiguration.InputValueType.SQUARE_METRES_GROSS));
        defaultOutputCategories.add(new OutputCategoryConfiguration(31, EMPLOYMENT_FLOORSPACE, "B8",
                OutputCategoryConfiguration.InputValueType.SQUARE_METRES_GROSS));
        defaultOutputCategories.add(new OutputCategoryConfiguration(32, EMPLOYMENT_FLOORSPACE, "C1",
                OutputCategoryConfiguration.InputValueType.BEDROOMS));
        defaultOutputCategories.add(new OutputCategoryConfiguration(33, EMPLOYMENT_FLOORSPACE, "D1",
                OutputCategoryConfiguration.InputValueType.SQUARE_METRES_GROSS));
        defaultOutputCategories.add(new OutputCategoryConfiguration(34, EMPLOYMENT_FLOORSPACE, "D2",
                OutputCategoryConfiguration.InputValueType.SQUARE_METRES_GROSS));

        defaultOutputCategories.add(new OutputCategoryConfiguration(35, DEVELOPMENT_VALUE, "N/A",
                OutputCategoryConfiguration.InputValueType.MONETARY_VALUE));

        List<OutputCategoryConfiguration> regenOutputCategories = new ArrayList<>();
        regenOutputCategories.add(new OutputCategoryConfiguration(101, ADDITIONAL_LEARNERS_SUPPORTED, "N/A",
                OutputCategoryConfiguration.InputValueType.NUMBER_OF));
        regenOutputCategories.add(new OutputCategoryConfiguration(102, COST_SAVINGS, "N/A",
                OutputCategoryConfiguration.InputValueType.MONETARY_VALUE));
        regenOutputCategories
                .add(new OutputCategoryConfiguration(103, ADDITIONAL_JOBS_CREATED, "Within the college or training provider",
                        OutputCategoryConfiguration.InputValueType.POSITIONS));
        regenOutputCategories
                .add(new OutputCategoryConfiguration(104, ADDITIONAL_JOBS_CREATED, "Within a supported business of the project",
                        OutputCategoryConfiguration.InputValueType.POSITIONS));
        regenOutputCategories.add(new OutputCategoryConfiguration(105, ADDITIONAL_APPRENTICESHIP_STARTS, "N/A",
                OutputCategoryConfiguration.InputValueType.POSITIONS));
        regenOutputCategories.add(new OutputCategoryConfiguration(106, ADDITIONAL_SEND_LEARNERS_SUPPORTED, "N/A",
                OutputCategoryConfiguration.InputValueType.POSITIONS));
        regenOutputCategories.add(new OutputCategoryConfiguration(107, ADDITIONAL_REDUCTION_IN_NEETS, "N/A",
                OutputCategoryConfiguration.InputValueType.NUMBER_OF));
        regenOutputCategories.add(new OutputCategoryConfiguration(108, ADDITIONAL_LEARNERS_THAT_WILL_NOT_BECOME_NEET, "N/A",
                OutputCategoryConfiguration.InputValueType.NUMBER_OF));
        regenOutputCategories.add(new OutputCategoryConfiguration(109, ADDITIONAL_STUDENTS_PROGRESSING_TO_EMPLOYMENT, "Full Time",
                OutputCategoryConfiguration.InputValueType.NUMBER_OF));
        regenOutputCategories.add(new OutputCategoryConfiguration(110, ADDITIONAL_STUDENTS_PROGRESSING_TO_EMPLOYMENT, "Part Time",
                OutputCategoryConfiguration.InputValueType.NUMBER_OF));
        OutputCategoryConfiguration hiddenCategory = new OutputCategoryConfiguration(HIDDEN_OUTPUT_CATEGORY_ID, "Test Hidden",
                "Hidden", OutputCategoryConfiguration.InputValueType.NUMBER_OF);
        hiddenCategory.setHidden(true);
        regenOutputCategories.add(hiddenCategory);

        int esfCategoryId = 201;
        List<String> categories = Arrays.asList(
                "P1.1: Access to Employment",
                "P1.2: Integration of young people",
                "P2.1: Access to Lifelong Learning",
                "P2.2: Education and Training Systems"
        );

        List<OutputCategoryConfiguration> skillsEsfOutputCategories = new ArrayList<>();
        for (String category : categories) {
            skillsEsfOutputCategories
                    .add(new OutputCategoryConfiguration(esfCategoryId++, category, "Starters (participants recruited)",
                            OutputCategoryConfiguration.InputValueType.OUTPUTS));
            skillsEsfOutputCategories.add(new OutputCategoryConfiguration(esfCategoryId++, category,
                    "Participants Achieving Qualifications (Level 2 & Level 3)",
                    OutputCategoryConfiguration.InputValueType.OUTPUTS));
            skillsEsfOutputCategories.add(new OutputCategoryConfiguration(esfCategoryId++, category,
                    "Participants achieving Qualifications (Two Level 3 units)",
                    OutputCategoryConfiguration.InputValueType.RESULTS));
            skillsEsfOutputCategories.add(new OutputCategoryConfiguration(esfCategoryId++, category,
                    "Participants achieving Qualifications (Level 2 and two Level 3 units)",
                    OutputCategoryConfiguration.InputValueType.OUTPUTS));
            skillsEsfOutputCategories
                    .add(new OutputCategoryConfiguration(esfCategoryId++, category, "Participants gaining Entry to Employment",
                            OutputCategoryConfiguration.InputValueType.OUTPUTS));
            skillsEsfOutputCategories.add(new OutputCategoryConfiguration(esfCategoryId++, category,
                    "Participants gaining Entry to Education or Training", OutputCategoryConfiguration.InputValueType.OUTPUTS));
            skillsEsfOutputCategories.add(new OutputCategoryConfiguration(esfCategoryId++, category,
                    "Participants Sustaining Employment for 26 weeks (out of 32)",
                    OutputCategoryConfiguration.InputValueType.OUTPUTS));
            skillsEsfOutputCategories.add(new OutputCategoryConfiguration(esfCategoryId++, category,
                    "Participants Sustaining Education or Training for 26 weeks (out of 32)",
                    OutputCategoryConfiguration.InputValueType.OUTPUTS));
        }

        List<OutputCategoryConfiguration> ggfOutputCategories = new ArrayList<>();
        ggfOutputCategories.add(new OutputCategoryConfiguration(112, "Number of people who participate in a project", "N/A",
                OutputCategoryConfiguration.InputValueType.NUMBER_OF));
        ggfOutputCategories.add(new OutputCategoryConfiguration(113, "Number of volunteering opportunities created", "N/A",
                OutputCategoryConfiguration.InputValueType.NUMBER_OF));
        ggfOutputCategories.add(new OutputCategoryConfiguration(114, "Sense of belonging to an area", "N/A",
                OutputCategoryConfiguration.InputValueType.NUMBER_OF));
        ggfOutputCategories.add(new OutputCategoryConfiguration(115, "Number of people progressing into work", "N/A",
                OutputCategoryConfiguration.InputValueType.NUMBER_OF));
        ggfOutputCategories
                .add(new OutputCategoryConfiguration(116, "The amount of public realm being created or improved", "N/A",
                        OutputCategoryConfiguration.InputValueType.NUMBER_OF));
        ggfOutputCategories.add(new OutputCategoryConfiguration(117, "Number of vacant units being brought back into use", "N/A",
                OutputCategoryConfiguration.InputValueType.NUMBER_OF));
        ggfOutputCategories.add(new OutputCategoryConfiguration(118, "Increase in visitor satisfaction", "N/A",
                OutputCategoryConfiguration.InputValueType.NUMBER_OF));
        ggfOutputCategories.add(new OutputCategoryConfiguration(119, "Increase in footfall", "N/A",
                OutputCategoryConfiguration.InputValueType.NUMBER_OF));
        ggfOutputCategories
                .add(new OutputCategoryConfiguration(120, "New jobs being created and existing jobs being safeguarded", "N/A",
                        OutputCategoryConfiguration.InputValueType.NUMBER_OF));
        ggfOutputCategories.add(new OutputCategoryConfiguration(121, "Number of businesses receiving support", "N/A",
                OutputCategoryConfiguration.InputValueType.NUMBER_OF));
        ggfOutputCategories.add(new OutputCategoryConfiguration(122, "Commercial space being created/improved", "N/A",
                OutputCategoryConfiguration.InputValueType.NUMBER_OF));
        ggfOutputCategories.add(new OutputCategoryConfiguration(123, "Increase in business turnover", "N/A",
                OutputCategoryConfiguration.InputValueType.NUMBER_OF));

        OutputCategoryConfiguration noGroupCategory = new OutputCategoryConfiguration(999, "Test No Group", "N/A",
                OutputCategoryConfiguration.InputValueType.NUMBER_OF);
        configurationRepository.save(noGroupCategory);

        configurationRepository.saveAll(defaultOutputCategories);
        configurationRepository.saveAll(regenOutputCategories);
        configurationRepository.saveAll(skillsEsfOutputCategories);
        configurationRepository.saveAll(ggfOutputCategories);
        outputTypeRepository.saveAll(defaultOutputTypes);
        outputTypeRepository.saveAll(regenOutputTypes);
        outputTypeRepository.saveAll(ggfOutputTypes);

        createOutputConfigGroup(DEFAULT_CONFIG_GROUP_ID, defaultOutputCategories, defaultOutputTypes,
                OutputConfigurationGroup.PeriodType.Monthly, "Output Type", "Category", "Sub Category");
        createOutputConfigGroup(REGEN_CONFIG_GROUP_ID, regenOutputCategories, regenOutputTypes,
                OutputConfigurationGroup.PeriodType.Quarterly, "Output Type", "Category", "Sub Category");
        createOutputConfigGroup(SINGLE_OUTPUT_GROUP_ID, defaultOutputCategories, singleOutputType,
                OutputConfigurationGroup.PeriodType.Quarterly, "Output Type", "Category", "Sub Category");
        createOutputConfigGroup(SKILLS_ESF_OUTPUT_GROUP_ID, skillsEsfOutputCategories, null,
                OutputConfigurationGroup.PeriodType.Monthly, null, null, null);
        createOutputConfigGroup(GOOD_GROWTH_FUND_OUTPUT_GROUP_ID, ggfOutputCategories, ggfOutputTypes,
                OutputConfigurationGroup.PeriodType.Quarterly, "Output Type", "Category", null);
    }

    private void createOutputConfigGroup(int id, List<OutputCategoryConfiguration> categories, List<OutputType> types,
            OutputConfigurationGroup.PeriodType period, String outputTypeName, String categoryName, String subCategoryName) {
        OutputConfigurationGroup outputConfigGroup = new OutputConfigurationGroup();
        outputConfigGroup.setId(id);
        outputConfigGroup.setCategories(categories);
        outputConfigGroup.setOutputTypes(types);
        outputConfigGroup.setPeriodType(period);
        outputConfigGroup.setOutputTypeName(outputTypeName);
        outputConfigGroup.setCategoryName(categoryName);
        outputConfigGroup.setSubcategoryName(subCategoryName);
        outputConfigurationGroupRepository.save(outputConfigGroup);
    }

    @Override
    public int executionOrder() {
        return 0;
    }

}
