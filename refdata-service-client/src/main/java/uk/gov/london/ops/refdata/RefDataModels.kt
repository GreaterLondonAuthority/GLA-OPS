/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.refdata

import java.time.OffsetDateTime

class FinanceCategory(var id: Int? = null,
                      var text: String? = null,
                      var spendStatus: FinanceCategoryStatus? = null,
                      var receiptStatus: FinanceCategoryStatus? = null,
                      var modifiedOn: OffsetDateTime? = null,
                      var modifiedBy: String? = null,
                      var ceCodes: List<CECode> = ArrayList()) {
    constructor(id: Int? = null, text: String? = null, spendStatus: FinanceCategoryStatus? = null, receiptStatus: FinanceCategoryStatus? = null)
    : this(id, text, spendStatus, receiptStatus, null, null)
}

class CECode(var id: Int? = null,
             var financeCategoryId: Int? = null)

class ConfigurableListItemGroupUsage(val templateId: Int,
                                     val templateName: String,
                                     val templateStatus: String)

class ConfigurableListItemGroup(var externalId: Int? = null,
                                var categories: List<ConfigurableListItem>? = ArrayList(),
                                var usage: List<ConfigurableListItemGroupUsage> = ArrayList()) {
}

class ConfigurableListItem(var id: Int? = null,
                           var externalId: Int? = null,
                           var category: String? = null,
                           var displayOrder: Int? = null,
                           val type:ConfigurableListItemType = ConfigurableListItemType.BudgetCategories) {

}

enum class ConfigurableListItemType { BudgetCategories}

class Borough(var id: Int? = null,
              var displayOrder: Int? = null,
              var boroughName: String? = null,
              var wards: List<Ward?>? = ArrayList())

class Ward(var id: Int? = null,
           var displayOrder: Int? = null,
           var wardName: String? = null)

