/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.refdata;

import static uk.gov.london.ops.framework.OPSUtils.currentUsername;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.london.ops.framework.exception.ValidationException;
import uk.gov.london.ops.refdata.implementation.repository.FinanceCategoryRepository;

/**
 * Service interface for managing SAP cat codes.
 *
 * @author Chris
 */
@Service
@Transactional
public class FinanceCategoryService {

    @Autowired
    FinanceCategoryRepository financeCategoryRepository;

    public List<FinanceCategoryEntity> getFinanceCategories(String categoryName) {
        List<FinanceCategoryEntity> all = financeCategoryRepository.findAll();
        if(categoryName != null) {
            return all.stream().filter(category -> category.getText().toLowerCase().contains(categoryName.toLowerCase())).collect(Collectors.toList());
        } else {
            return all;
        }
    }

    public FinanceCategoryEntity createFinanceCategory(FinanceCategoryEntity category) {
        checkCeCodesAreUnique(category);
        FinanceCategoryEntity updated = financeCategoryRepository.save(category);
        for (CECodeEntity code : updated.getCeCodes()) {
            code.setFinanceCategoryId(updated.getId());
        }
        return financeCategoryRepository.save(category);
    }

    public FinanceCategoryEntity updateFinanceCategory(Integer id, FinanceCategoryEntity category) {
        if (!id.equals(category.getId())) {
            throw new ValidationException("Updated id must match category id");
        }
        checkCeCodesAreUnique(category);
        category.setModifiedBy(currentUsername());
        return financeCategoryRepository.save(category);
    }

    void checkCeCodesAreUnique(FinanceCategoryEntity category) {
        boolean isCreate = category.getId() == null;
        for (CECodeEntity ceCode : category.getCeCodes()) {
            FinanceCategoryEntity byCeCode = financeCategoryRepository.findByCeCode(ceCode.getId());
            if (byCeCode != null) {
                if (isCreate || !byCeCode.getId().equals(category.getId())) {
                    throw new ValidationException(
                            "Code " + ceCode.getId() + " is already in use by category " + byCeCode.getText());
                }
            }
        }
    }
}
