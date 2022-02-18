/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.refdata;

import java.util.List;
import java.util.Map;

public interface RefDataService {

    List<MarketType> getMarketTypes();

    MarketType getMarketType(Integer id);

    MarketType getMarketTypeByName(String name);

    CategoryValue getCategoryValue(Integer id);

    List<CategoryValue> getCategoryValues(CategoryValue.Category category);

    FinanceCategory getFinanceCategory(Integer id);

    FinanceCategory getFinanceCategoryByCeCode(Integer ceCode);

    FinanceCategory getFinanceCategoryByText(String categoryText);

    Map<String, PaymentSource> getPaymentSourceMap();

    List<ConfigurableListItem> getConfigurableListItemsByExtID(Integer externalId);

    TenureType getTenureType(Integer id);

    TenureType createTenureType(TenureType tenureType);

    List<Borough> getBoroughs();

    Borough findBoroughByName(String name);

    List<Ward> getWards();

}
