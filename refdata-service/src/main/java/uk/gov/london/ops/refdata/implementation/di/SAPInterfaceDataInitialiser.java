/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.refdata.implementation.di;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import uk.gov.london.common.CSVFile;
import uk.gov.london.ops.framework.di.DataInitialiserModule;
import uk.gov.london.ops.refdata.CECode;
import uk.gov.london.ops.refdata.FinanceCategory;
import uk.gov.london.ops.refdata.FinanceCategoryStatus;
import uk.gov.london.ops.refdata.implementation.repository.CECodeRepository;
import uk.gov.london.ops.refdata.implementation.repository.FinanceCategoryRepository;

import java.io.IOException;
import java.util.List;

/**
 * Initialise any data required for the SAP interface.
 *
 * Created by sleach on 04/09/2017.
 */
@Component
public class SAPInterfaceDataInitialiser implements DataInitialiserModule {

    Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    FinanceCategoryRepository financeCategoryRepository;

    @Autowired
    CECodeRepository ceCodeRepository;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Override
    public String getName() {
        return "SAP Interface data initialiser";
    }

    @Override
    public boolean runInAllEnvironments() {
        return true;
    }

    @Override
    public void addReferenceData() {
        try {
            createOpsCategoryCodes();
        } catch (Exception e) {
            log.error("failed to load ops category codes", e);
        }
    }

    private void createOpsCategoryCodes() throws IOException {
        if (financeCategoryRepository.count() == 0) {
            List<FinanceCategory> financeCategories = new CSVFile(this.getClass().getResourceAsStream("finance-categories.csv"))
                    .loadData(csv -> new FinanceCategory(
                            csv.getInteger("OPS Category Code"),
                            csv.getString("OPS Category"),
                            FinanceCategoryStatus.valueOf(csv.getString("Spend Permission")),
                            FinanceCategoryStatus.valueOf(csv.getString("Receipts Permission"))
                    ));

            for (FinanceCategory financeCategory : financeCategories) {
                jdbcTemplate.update("insert into finance_category (id, text, spend_status, receipt_status) values (?,?,?,?)",
                        financeCategory.getId(), financeCategory.getText(), financeCategory.getSpendStatus().name(),
                        financeCategory.getReceiptStatus().name());
            }
            log.info("loaded OPS category codes");
        }

        if (ceCodeRepository.count() == 0) {
            List<CECode> ceCodesMapping = new CSVFile(this.getClass().getResourceAsStream("ce-codes-mapping.csv"))
                    .loadData(csv -> new CECode(csv.getInteger("CE Code"), csv.getInteger("OPS Code")
                    ));

            for (CECode code : ceCodesMapping) {
                jdbcTemplate.update("insert into ce_code (id, finance_category_id) values (?,?)",
                        code.getId(), code.getFinanceCategoryId());
            }
            log.info("loaded CE codes mapping");
        }
    }

}
