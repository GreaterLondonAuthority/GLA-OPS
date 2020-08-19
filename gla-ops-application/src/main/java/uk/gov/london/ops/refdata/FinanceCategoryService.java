/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.refdata;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.london.ops.framework.exception.ValidationException;
import uk.gov.london.ops.refdata.implementation.repository.FinanceCategoryRepository;
import uk.gov.london.ops.user.UserService;

/**
 * Service interface for managing SAP cat codes.
 *
 * @author Chris
 */
@Service
@Transactional
public class FinanceCategoryService {

    Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    FinanceCategoryRepository financeCategoryRepository;

    @Autowired
    private UserService userService;

    public List<FinanceCategory> getFinanceCategories() {
        return financeCategoryRepository.findAll();
    }

    public FinanceCategory createFinanceCategory(FinanceCategory category) {
        checkCeCodesAreUnique(category);
        FinanceCategory updated = financeCategoryRepository.save(category);
        for (CECode code : updated.getCeCodes()) {
            code.setFinanceCategoryId(updated.getId());
        }
        return financeCategoryRepository.save(category);
    }

    public FinanceCategory updateFinanceCategory(Integer id, FinanceCategory category) {
        if (!id.equals(category.getId())) {
            throw new ValidationException("Updated id must match category id");
        }
        checkCeCodesAreUnique(category);
        category.setModifiedByUser(userService.currentUser());
        return financeCategoryRepository.save(category);
    }

    private boolean checkCeCodesAreUnique(FinanceCategory category) {
        boolean isCreate = category.getId() == null;
        for (CECode ceCode : category.getCeCodes()) {
            FinanceCategory byCeCode = financeCategoryRepository.findByCeCode(ceCode.getId());
            if (byCeCode != null) {
                if (isCreate || !byCeCode.getId().equals(category.getId())) {
                    throw new ValidationException(
                            "Code " + ceCode.getId() + " is already in use by category " + byCeCode.getText());
                }
            }
        }
        return true;
    }
}
