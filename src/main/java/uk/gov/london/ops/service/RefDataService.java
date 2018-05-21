/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.london.ops.domain.refdata.Borough;
import uk.gov.london.ops.domain.refdata.CategoryValue;
import uk.gov.london.ops.domain.refdata.TenureType;
import uk.gov.london.ops.repository.BoroughRepository;
import uk.gov.london.ops.repository.CategoryValueRepository;
import uk.gov.london.ops.repository.TenureTypeRepository;

import java.util.List;

/**
 * Reference data service.
 *
 * Provides access to various forms of reference data.
 */
@Service
public class RefDataService {

    @Autowired
    TenureTypeRepository tenureTypeRepository;

    @Autowired
    CategoryValueRepository categoryValueRepository;

    @Autowired
    BoroughRepository boroughRepository;

    public List<TenureType> getTenureTypes() {
        return tenureTypeRepository.findAll();
    }

    public void createTenureType(TenureType tenureType) {
        tenureTypeRepository.save(tenureType);
    }

    public List<CategoryValue> getCategoryValues(CategoryValue.Category category) {
        return categoryValueRepository.findAllByCategoryOrderByDisplayOrder(category);
    }

    public CategoryValue getCategoryValue(Integer id) {
        return categoryValueRepository.findOne(id);
    }

    public List<Borough> getBoroughs() {
        return boroughRepository.findAll();

    }

}
