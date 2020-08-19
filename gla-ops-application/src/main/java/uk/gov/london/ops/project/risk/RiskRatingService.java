/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.risk;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.london.ops.framework.exception.NotFoundException;
import uk.gov.london.ops.framework.exception.ValidationException;
import uk.gov.london.ops.project.implementation.repository.RiskRatingRepository;
import uk.gov.london.ops.project.template.domain.RiskRating;

import java.util.List;

@Service
public class RiskRatingService {

    @Autowired
    private RiskRatingRepository riskRatingRepository;

    public List<RiskRating> findAll() {
        return riskRatingRepository.findAll();
    }

    public RiskRating find(Integer id) {
        RiskRating riskRating = riskRatingRepository.findById(id).orElse(null);
        if (riskRating == null) {
            throw new NotFoundException("Could not find risk rating");
        }
        return riskRating;
    }

    private void validateRiskRatingOption(RiskRating riskRating) {
        if (riskRating.getName() == null || riskRating.getName().isEmpty()) {
            throw new ValidationException("Risk rating name cannot be null or empty");
        }

        if (riskRating.getDescription() == null || riskRating.getDescription().isEmpty()) {
            throw new ValidationException("Risk rating description cannot be null or empty");
        }

        if (riskRating.getDisplayOrder() == null) {
            throw new ValidationException("Risk rating display order cannot be null");
        }

        if (riskRating.getColor() == null || riskRating.getColor().isEmpty()) {
            throw new ValidationException("Risk rating color cannot be null or empty");
        }
    }

    public RiskRating create(RiskRating riskRating) {
        if (riskRating.getId() != null) {
            throw new ValidationException("Risk rating ID should not be set!");
        }
        validateRiskRatingOption(riskRating);

        return riskRatingRepository.save(riskRating);
    }

    public void delete(RiskRating riskRating) {
        riskRatingRepository.delete(riskRating);
    }

}
