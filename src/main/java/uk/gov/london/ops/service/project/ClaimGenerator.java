/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

package uk.gov.london.ops.service.project;

import uk.gov.london.ops.domain.project.Claim;
import uk.gov.london.ops.domain.project.NamedProjectBlock;
import uk.gov.london.ops.domain.project.Project;
import uk.gov.london.ops.framework.exception.ValidationException;

public interface ClaimGenerator<T extends NamedProjectBlock> {
    /**
     *
     * @param project
     * @param block
     * @param claim
     * @throws ValidationException if claim can't be generated
     */
    void generateClaim(Project project, T block, Claim claim);

    /**
     * @param block
     * @param claim
     * @return if custom claim deletion logic exists it should be implemented in this method and return true
     */
    boolean handleClaimDeletion(T block, Claim claim);
}
