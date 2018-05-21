/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.london.ops.domain.refdata.FinanceCategory;
import uk.gov.london.ops.repository.FinanceCategoryRepository;

import java.util.List;

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

    public List<FinanceCategory> getFinanceCategories() {
        return financeCategoryRepository.findAll();
    }

}
