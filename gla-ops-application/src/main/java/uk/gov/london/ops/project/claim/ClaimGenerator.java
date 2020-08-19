/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

package uk.gov.london.ops.project.claim;

import uk.gov.london.ops.framework.exception.ValidationException;
import uk.gov.london.ops.project.Project;
import uk.gov.london.ops.project.block.NamedProjectBlock;

public interface ClaimGenerator<T extends NamedProjectBlock> {

    /**
     * @throws ValidationException if claim can't be generated
     */
    void generateClaim(Project project, T block, Claim claim);

    /**
     * @return if custom claim deletion logic exists it should be implemented in this method and return true
     */
    boolean handleClaimDeletion(T block, Claim claim);
}
