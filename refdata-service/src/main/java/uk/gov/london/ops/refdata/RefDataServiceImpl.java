/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.refdata;

import javax.transaction.Transactional;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import uk.gov.london.ops.framework.exception.ValidationException;
import uk.gov.london.ops.refdata.implementation.repository.BoroughRepository;
import uk.gov.london.ops.refdata.implementation.repository.CategoryValueRepository;
import uk.gov.london.ops.refdata.implementation.repository.ConfigurableListItemRepository;
import uk.gov.london.ops.refdata.implementation.repository.FinanceCategoryRepository;
import uk.gov.london.ops.refdata.implementation.repository.MarketTypeRepository;
import uk.gov.london.ops.refdata.implementation.repository.PaymentSourceRepository;
import uk.gov.london.ops.refdata.implementation.repository.TenureTypeRepository;
import uk.gov.london.ops.refdata.implementation.repository.WardRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Reference data service.
 *
 * Provides access to various forms of reference data.
 */
@Transactional
@Service
public class RefDataServiceImpl implements RefDataService {

    @Autowired
    BoroughRepository boroughRepository;

    @Autowired
    CategoryValueRepository categoryValueRepository;

    @Autowired
    ConfigurableListItemRepository configurableListItemRepository;

    @Autowired
    FinanceCategoryRepository financeCategoryRepository;

    @Autowired
    MarketTypeRepository marketTypeRepository;

    @Autowired
    TenureTypeRepository tenureTypeRepository;

    @Autowired
    PaymentSourceRepository paymentSourceRepository;

    @Autowired
    WardRepository wardRepository;

    @Autowired
    JdbcTemplate jdbc;

    private Map<String, PaymentSource> paymentSourceMap = null;

    public Map<String, PaymentSource> getPaymentSourceMap() {
        if (paymentSourceMap == null) {
            paymentSourceMap = paymentSourceRepository.findAll()
                    .stream()
                    .map(this::toModel)
                    .collect(Collectors.toMap(PaymentSource::getName, Function.identity()));
        }
        return paymentSourceMap;
    }

    public List<Borough> getBoroughs() {
        return boroughRepository.findAll().stream().map(this::toModel).collect(Collectors.toList());
    }

    public Borough findBoroughByName(String borough) {
        return toModel(boroughRepository.findByBoroughName(borough));
    }

    public List<Ward> getWards() {
        return wardRepository.findAll().stream().map(this::toModel).collect(Collectors.toList());
    }

    public List<CategoryValue> getCategoryValues(CategoryValue.Category category) {
        return categoryValueRepository.findAllByCategoryOrderByDisplayOrder(category);
    }

    public CategoryValue getCategoryValue(Integer id) {
        return categoryValueRepository.findById(id).orElse(null);
    }

    public CategoryValue findByCategoryAndDisplayValue(CategoryValue.Category category, String displayValue) {
        return categoryValueRepository.findByCategoryAndDisplayValue(category, displayValue);
    }

    public List<ConfigurableListItemGroupUsage> getConfigItemGroupUsage(Integer externalId) {
        String sql = null;
        if (externalId != null) {
            sql = "select distinct t.id, t.name, t.template_status from template_block tb "
                    + "inner join template t on t.id = tb.template_id "
                    + "where tb.category_external_id = " + externalId;
        }

        if (sql != null) {
            return jdbc.query(sql, (resultSet, i) -> new ConfigurableListItemGroupUsage(resultSet.getInt(1), resultSet.getString(2), resultSet.getString(3)));
        } else {
            return Collections.emptyList();
        }
    }

    public List<ConfigurableListItemGroup> getConfigurableItemsGroups(String type, String categoryName) {
        List<ConfigurableListItemGroup> groups = new ArrayList<>();
        Map<Integer, List<ConfigurableListItemEntity>> items = configurableListItemRepository.findAllByType(ConfigurableListItemType.valueOf(type))
                .stream().collect(Collectors.groupingBy(ConfigurableListItemEntity::getExternalId));
        for(Map.Entry<Integer, List<ConfigurableListItemEntity>> entry: items.entrySet()) {
            List<ConfigurableListItemGroupUsage> entryUsage = getConfigItemGroupUsage(entry.getKey());
            List<ConfigurableListItem> categories = entry.getValue().stream()
                    .map(i -> new ConfigurableListItem(i.getId(), i.getExternalId(), i.getCategory(), i.getDisplayOrder(), i.getType()))
                    .collect(Collectors.toList());
            // Partial match search by category name and only add to groups if any category exists
            if(categoryName != null) {
                categories = categories.stream().filter(category -> category.getCategory().toLowerCase().contains(categoryName.toLowerCase()))
                        .collect(Collectors.toList());
            }
            if(!categories.isEmpty()) {
                groups.add(new ConfigurableListItemGroup(entry.getKey(), categories, entryUsage));
            }
        }
        return groups;
    }

    public Map<Integer, List<ConfigurableListItemEntity>> getConfigurableGroupIds() {
        return configurableListItemRepository.findAll().stream().collect(Collectors.groupingBy(ConfigurableListItemEntity::getExternalId));
    }

    public List<ConfigurableListItem> getConfigurableListItemsByExtID(Integer externalId) {
        return configurableListItemRepository.findAllByExternalIdOrderByDisplayOrder(externalId)
                .stream()
                .map(this::toModel)
                .collect(Collectors.toList());
    }

    public List<ConfigurableListItemEntity> createConfigurableListItems(List<ConfigurableListItemEntity> items) {
        return configurableListItemRepository.saveAll(items);
    }

    public void deleteConfigurableListItem(Integer externalId, Integer categoryId) {
        validateConfigListItemsBeforeDelete(externalId);
        configurableListItemRepository.deleteById(categoryId);
    }

    public void deleteConfigurableListItemGroup(Integer externalId) {
        validateConfigListItemsBeforeDelete(externalId);
        configurableListItemRepository.deleteAllByExternalId(externalId);
    }

    void validateConfigListItemsBeforeDelete(Integer externalId) {
        List<ConfigurableListItemGroupUsage> usage = getConfigItemGroupUsage(externalId);
        boolean active = usage.stream().anyMatch(template -> template.getTemplateStatus().equalsIgnoreCase("Active"));

        if (active) {
            throw new ValidationException("Cannot delete a category group used in a template!");
        }
    }

    public FinanceCategory getFinanceCategory(Integer id) {
        return toModel(financeCategoryRepository.findById(id).orElse(null));
    }

    public FinanceCategory getFinanceCategoryByText(String text) {
        return toModel(financeCategoryRepository.findFirstByText(text));
    }

    public FinanceCategory getFinanceCategoryByCeCode(Integer ceCode) {
        return toModel(financeCategoryRepository.findByCeCode(ceCode));
    }

    public List<MarketType> getMarketTypes() {
        return marketTypeRepository.findAll();
    }

    public MarketType getMarketType(Integer id) {
        return marketTypeRepository.findById(id).orElse(null);
    }

    public MarketType getMarketTypeByName(String name) {
        return marketTypeRepository.findByName(name);
    }

    public List<TenureType> getTenureTypes() {
        return tenureTypeRepository.findAll();
    }

    public TenureType getTenureType(Integer id) {
        return tenureTypeRepository.findById(id).orElse(null);
    }

    public TenureType createTenureType(TenureType tenureType) {
        return tenureTypeRepository.save(tenureType);
    }

    public TenureType updateTenureType(TenureType tenureType) {
        return tenureTypeRepository.save(tenureType);
    }

    public Set<PaymentSourceEntity> getAvailablePaymentSources() {
        return new HashSet<>(paymentSourceRepository.findAll());
    }

    @Scheduled(cron = "${paymentsource.cache.expiry.scheduler.cron.expression}")
    public void expireCache() {
        this.paymentSourceMap = null;
    }

    public PaymentSourceEntity createPaymentSource(PaymentSourceEntity paymentSource) {
        if (StringUtils.isEmpty(paymentSource.getName())
                || StringUtils.isEmpty(paymentSource.getDescription())
                || paymentSource.getGrantType() == null) {
            throw new ValidationException("Ensure all fields are present (name, description and grantType");
        }
        if (getPaymentSourceMap().get(paymentSource.getName()) != null) {
            throw new ValidationException(String.format("Payment source with name %s already exists", paymentSource.getName()));
        }

        this.paymentSourceMap = null;
        return paymentSourceRepository.save(paymentSource);
    }

    private PaymentSource toModel(PaymentSourceEntity entity) {
        if (entity != null) {
            return new PaymentSource(entity.getName(), entity.getDescription(), entity.getGrantType(), entity.isSendToSap());
        } else {
            return null;
        }
    }

    private Borough toModel(BoroughEntity entity) {
        if (entity != null) {
            Borough model = new Borough();
            model.setId(entity.getId());
            model.setDisplayOrder(entity.getDisplayOrder());
            model.setBoroughName(entity.getBoroughName());
            model.setWards(entity.getWards().stream().map(this::toModel).collect(Collectors.toList()));
            return model;
        } else {
            return null;
        }
    }

    private Ward toModel(WardEntity entity) {
        Ward model = new Ward();
        if (entity != null) {
            model.setId(entity.getId());
            model.setDisplayOrder(entity.getDisplayOrder());
            model.setWardName(entity.getWardName());
        }
        return model;
    }

    private ConfigurableListItem toModel(ConfigurableListItemEntity entity) {
        ConfigurableListItem model = new ConfigurableListItem();
        if (entity != null) {
            model.setId(entity.getId());
            model.setExternalId(entity.getExternalId());
            model.setCategory(entity.getCategory());
            model.setDisplayOrder(entity.getDisplayOrder());
        }
        return model;
    }

    private FinanceCategory toModel(FinanceCategoryEntity entity) {
        FinanceCategory model = new FinanceCategory();
        if (entity != null) {
            model.setId(entity.getId());
            model.setText(entity.getText());
            model.setSpendStatus(entity.getSpendStatus());
            model.setReceiptStatus(entity.getReceiptStatus());
            model.setModifiedOn(entity.getModifiedOn());
            model.setModifiedBy(entity.getModifiedBy());
            model.setCeCodes(entity.getCeCodes().stream().map(this::toModel).collect(Collectors.toList()));
        }
        return model;
    }

    private CECode toModel(CECodeEntity entity) {
        return entity != null ? new CECode(entity.getId(), entity.getFinanceCategoryId()) : null;
    }


    public void updateConfigurableListItem(Integer id, ConfigurableListItemEntity configItem) {
        if (id == null || !id.equals(configItem.getId())) {
            throw new ValidationException("Item ID not valid.");
        }

        ConfigurableListItemEntity entity = configurableListItemRepository.getOne(id);
        entity.setCategory(configItem.getCategory());
        entity.setDisplayOrder(configItem.getDisplayOrder());
        configurableListItemRepository.save(entity);
    }
}
