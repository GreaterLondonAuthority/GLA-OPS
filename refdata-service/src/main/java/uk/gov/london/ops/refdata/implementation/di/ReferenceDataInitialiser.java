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
import uk.gov.london.ops.framework.enums.GrantType;
import uk.gov.london.ops.framework.environment.Environment;
import uk.gov.london.ops.refdata.*;
import uk.gov.london.ops.refdata.implementation.repository.CategoryValueRepository;
import uk.gov.london.ops.refdata.implementation.repository.MarketTypeRepository;
import uk.gov.london.ops.refdata.implementation.repository.PaymentSourceRepository;
import uk.gov.london.ops.refdata.implementation.repository.TenureTypeRepository;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.london.ops.refdata.PaymentSourceKt.*;
import static uk.gov.london.ops.refdata.TenureType.*;

/**
 * Initialises some basic reference data in all environments (not just test environments).
 *
 * Includes market and tenure types from unit details block, and risk levels from risks block.
 *
 * @author Steve Leach
 */
@Component
public class ReferenceDataInitialiser implements DataInitialiserModule {

    @Autowired
    TenureTypeRepository tenureTypeRepository;
    @Autowired
    MarketTypeRepository marketTypeRepository;
    @Autowired
    CategoryValueRepository categoryValueRepository;
    @Autowired
    RefDataServiceImpl refDataService;
    @Autowired
    Environment environment;

    @Autowired
    PaymentSourceRepository paymentSourceRepository;

    @Override
    public String getName() {
        return "Reference data initialiser";
    }

    @Override
    public boolean runInAllEnvironments() {
        return true;
    }

    @Override
    public void addReferenceData() {
        createTenureAndMarketTypeData();
        createCategoryValues();
        createConfigListItemsForFundingBlock();
        createPaymentSources();
    }

    private void createConfigListItemsForFundingBlock() {
        List<ConfigurableListItem> items = refDataService.getConfigurableListItemsByExtID(1000);
        if (items == null || items.isEmpty()) {
            List<ConfigurableListItemEntity> clis = new ArrayList<>();
            clis.add(new ConfigurableListItemEntity(1000, "Direct staff costs", 0, ConfigurableListItemType.BudgetCategories));
            clis.add(new ConfigurableListItemEntity(1000, "Indirect staff costs", 1, ConfigurableListItemType.BudgetCategories));
            clis.add(new ConfigurableListItemEntity(1000, "Other staffing and volunteer costs", 2, ConfigurableListItemType.BudgetCategories));
            clis.add(new ConfigurableListItemEntity(1000, "Other direct delivery costs", 3, ConfigurableListItemType.BudgetCategories));
            clis.add(new ConfigurableListItemEntity(1000, "Premises costs", 4, ConfigurableListItemType.BudgetCategories));
            clis.add(new ConfigurableListItemEntity(1000, "Other indirect costs", 5, ConfigurableListItemType.BudgetCategories));
            clis.add(new ConfigurableListItemEntity(1001, "Direct costs", 0, ConfigurableListItemType.BudgetCategories));
            clis.add(new ConfigurableListItemEntity(1001, "Indirect costs", 1, ConfigurableListItemType.BudgetCategories));
            clis.add(new ConfigurableListItemEntity(1002, "Not in use category", 0, ConfigurableListItemType.BudgetCategories));

            refDataService.createConfigurableListItems(clis);
        }
    }

    public void createTenureAndMarketTypeData() {
        int id = 1;
        int displayOrder = 1;

        // be cautious modifying these values, check for existing data etc
        MarketType rentProduct = marketTypeRepository
                .save(new MarketType(id++, displayOrder++, "2016 - 2021 Rent Product", true, false));
        MarketType legacyRentProduct = marketTypeRepository
                .save(new MarketType(id++, displayOrder++, "Legacy Rent Product", true, false));
        MarketType salesProduct = marketTypeRepository.save(new MarketType(id++, displayOrder++, "Sales Product", false, true));
        MarketType discountedMarketRate = marketTypeRepository
                .save(new MarketType(id++, displayOrder++, "Discounted Market Rate", false, true));
        MarketType legacySalesProduct = marketTypeRepository
                .save(new MarketType(id++, displayOrder++, "Legacy Sales Product", false, true));

        if (tenureTypeRepository.count() == 0) {
            tenureTypeRepository.save(new TenureType(LONDON_AFFORDABLE_RENT_TENURE_ID, LONDON_AFFORDABLE_RENT, rentProduct));
            tenureTypeRepository.save(new TenureType(LONDON_LIVING_RENT_TENURE_ID, LONDON_LIVING_RENT, rentProduct));
            tenureTypeRepository.save(new TenureType(LONDON_SHARED_OWNERSHIP_TENURE_ID, LONDON_SHARED_OWNERSHIP, salesProduct));
            tenureTypeRepository
                    .save(new TenureType(OTHER_AFFORDABLE_TENURE_ID, OTHER_AFFORDABLE, rentProduct, legacyRentProduct, salesProduct,
                            discountedMarketRate));
            tenureTypeRepository.save(new TenureType(AFFORDABLE_RENT_ID, AFFORDABLE_RENT, legacyRentProduct));
            tenureTypeRepository.save(new TenureType(AFFORDABLE_HOME_OWNERSHIP_ID, AFFORDABLE_HOME_OWNERSHIP, discountedMarketRate));
            tenureTypeRepository.save(new TenureType(LEGACY_SHARED_OWNERSHIP_TENURE_ID, LEGACY_SHARED_OWNERSHIP, legacySalesProduct));
            tenureTypeRepository.save(new TenureType(AHP_LAR_ABOVE_BENCHMARK_TENURE_ID, AHP_LAR_ABOVE_BENCHMARK, discountedMarketRate));
        }
    }

    public void createPaymentSources() {
        paymentSourceRepository.save(new PaymentSourceEntity(GRANT, "Grant", GrantType.Grant, true));
        paymentSourceRepository.save(new PaymentSourceEntity(DPF, "Disposal Proceeds Fund (DPF)", GrantType.DPF, false));
        paymentSourceRepository.save(new PaymentSourceEntity(RCGF, "Recycled Capital Grant Fund (RCGF)", GrantType.RCGF, false));
        paymentSourceRepository
                .save(new PaymentSourceEntity(MOPAC, "Mayor's Office for Policing And Crime (MOPAC)", GrantType.Grant, false));
        paymentSourceRepository.save(new PaymentSourceEntity(ESF, "European Social Fund (ESF)", GrantType.Grant, true));
        paymentSourceRepository.save(new PaymentSourceEntity(BSF, "London Business Support Fund", GrantType.Grant, false));
    }

    public void createCategoryValues() {

        int id = 1;
        int displayOrder = 1;

        // be cautious modifying these values, check for existing data etc

        // Bedrooms
        categoryValueRepository.save(new CategoryValue(id++, CategoryValue.Category.Bedrooms, "Studio", displayOrder++));
        categoryValueRepository.save(new CategoryValue(id++, CategoryValue.Category.Bedrooms, "1", displayOrder++));
        categoryValueRepository.save(new CategoryValue(id++, CategoryValue.Category.Bedrooms, "2", displayOrder++));
        categoryValueRepository.save(new CategoryValue(id++, CategoryValue.Category.Bedrooms, "3", displayOrder++));
        categoryValueRepository.save(new CategoryValue(id++, CategoryValue.Category.Bedrooms, "4", displayOrder++));
        categoryValueRepository.save(new CategoryValue(id++, CategoryValue.Category.Bedrooms, "5", displayOrder++));
        categoryValueRepository.save(new CategoryValue(id++, CategoryValue.Category.Bedrooms, "6+", displayOrder++));

        displayOrder = 1;
        // Unit Types
        categoryValueRepository.save(new CategoryValue(id++, CategoryValue.Category.UnitTypes, "Flat", displayOrder++));
        categoryValueRepository.save(new CategoryValue(id++, CategoryValue.Category.UnitTypes, "Maisonette", displayOrder++));
        categoryValueRepository.save(new CategoryValue(id++, CategoryValue.Category.UnitTypes, "House", displayOrder++));
        categoryValueRepository.save(new CategoryValue(id++, CategoryValue.Category.UnitTypes, "Bungalow", displayOrder++));

        displayOrder = 1;
        // Payment Decline Reasons
        categoryValueRepository
                .save(new CategoryValue(id++, CategoryValue.Category.PaymentDeclineReason, "Incorrect payment amount",
                        displayOrder++));
        categoryValueRepository
                .save(new CategoryValue(id++, CategoryValue.Category.PaymentDeclineReason, "Project circumstances have changed",
                        displayOrder++));
        categoryValueRepository.save(new CategoryValue(id++, CategoryValue.Category.PaymentDeclineReason,
                "An error in a block approval has been identified", displayOrder++));
        categoryValueRepository.save(new CategoryValue(id++, CategoryValue.Category.PaymentDeclineReason,
                "The need for additional grant for the project has been identified", displayOrder++));
        categoryValueRepository
                .save(new CategoryValue(id++, CategoryValue.Category.PaymentDeclineReason, "Milestone claim approval error",
                        displayOrder++));
        categoryValueRepository
                .save(new CategoryValue(id++, CategoryValue.Category.PaymentDeclineReason, "Partner qualification failure",
                        displayOrder++));
        categoryValueRepository
                .save(new CategoryValue(id++, CategoryValue.Category.PaymentDeclineReason, "Project is no longer proceeding",
                        displayOrder++));
        categoryValueRepository.save(new CategoryValue(id++, CategoryValue.Category.PaymentDeclineReason,
                "Project no longer qualifies for the payment", displayOrder++));
        categoryValueRepository
                .save(new CategoryValue(id++, CategoryValue.Category.PaymentDeclineReason, "Other", displayOrder++));

        displayOrder = 1;
        // Risk Categories
        categoryValueRepository.save(new CategoryValue(id++, CategoryValue.Category.RiskCategory, "Capacity", displayOrder++));
        categoryValueRepository
                .save(new CategoryValue(id++, CategoryValue.Category.RiskCategory, "Environmental", displayOrder++));
        categoryValueRepository.save(new CategoryValue(id++, CategoryValue.Category.RiskCategory, "Financial", displayOrder++));
        categoryValueRepository
                .save(new CategoryValue(id++, CategoryValue.Category.RiskCategory, "Health and Safety", displayOrder++));
        categoryValueRepository.save(new CategoryValue(id++, CategoryValue.Category.RiskCategory, "Legal", displayOrder++));
        categoryValueRepository
                .save(new CategoryValue(id++, CategoryValue.Category.RiskCategory, "Outputs and Targets", displayOrder++));
        categoryValueRepository.save(new CategoryValue(id++, CategoryValue.Category.RiskCategory, "Other", displayOrder++));
        categoryValueRepository.save(new CategoryValue(id++, CategoryValue.Category.RiskCategory, "Reputation", displayOrder++));
        categoryValueRepository
                .save(new CategoryValue(id++, CategoryValue.Category.RiskCategory, "Timescale Delivery", displayOrder++));

        displayOrder = 1;
        // Reclaim Decline Reasons
        categoryValueRepository
                .save(new CategoryValue(id++, CategoryValue.Category.ReclaimDeclineReason, "Incorrect reclaim amount",
                        displayOrder++));
        categoryValueRepository
                .save(new CategoryValue(id++, CategoryValue.Category.ReclaimDeclineReason, "Project circumstances have changed",
                        displayOrder++));
        categoryValueRepository.save(new CategoryValue(id++, CategoryValue.Category.ReclaimDeclineReason,
                "An error in a block approval has been identified", displayOrder++));
        categoryValueRepository
                .save(new CategoryValue(id++, CategoryValue.Category.ReclaimDeclineReason, "Other", displayOrder++));
    }

}
