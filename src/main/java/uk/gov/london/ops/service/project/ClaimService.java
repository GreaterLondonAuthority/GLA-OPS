/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.service.project;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.london.ops.domain.project.*;
import uk.gov.london.ops.repository.ClaimRepository;
import uk.gov.london.ops.framework.exception.ValidationException;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Service
@Transactional
public class ClaimService extends BaseProjectService {

    @Autowired
    ClaimRepository claimRepository;

    @Autowired
    ProjectSkillsService projectSkillsService;

    @Autowired
    ProjectOutputsService projectOutputsService;

    Map<ProjectBlockType, ClaimGenerator> claimGenerators = new HashMap<>();

    @PostConstruct
    public void postConstruct() {
        claimGenerators.put(ProjectBlockType.LearningGrant, projectSkillsService);
        claimGenerators.put(ProjectBlockType.Outputs, projectOutputsService);
    }

    public void createClaim(Integer projectId, Integer blockId, Claim claim) {
        Project project = get(projectId);
        NamedProjectBlock blockById = project.getProjectBlockById(blockId);
        NamedProjectBlock block = get(projectId).getSingleLatestBlockOfType(blockById.getBlockType());
        if (block == null || block.getId() == null || !block.getId().equals(blockId)) {
            throw new ValidationException("Block not found: " + blockById);
        }
        checkForLock(block);

        ClaimGenerator claimGenerator = claimGenerators.get(block.getBlockType());
        if (claimGenerator == null) {
            throw new ValidationException("Claim is not currently supported for " + block.getBlockType());
        }

        claimGenerator.generateClaim(project, block, claim);
        claim.setBlockId(block.getId());
        claim.setClaimStatus(ClaimStatus.Claimed);
        claim.setClaimedOn(environment.now());

        this.updateProject(project);
    }

    public void deleteClaim(Integer projectId, Integer blockId, Integer claimId) {
        Project project = get(projectId);
        NamedProjectBlock blockById = project.getProjectBlockById(blockId);

        NamedProjectBlock block = project.getSingleLatestBlockOfType(blockById.getBlockType());
        if (!blockId.equals(block.getId())) {
            throw new ValidationException("Unable to find block with specified ID");
        }
        checkForLock(block);
        Claim claim = claimRepository.findById(claimId).orElse(null);
        if (claim == null || !blockId.equals(claim.getBlockId())) {
            throw new ValidationException("Claim not found");
        }

        boolean wasClaimDeletionHandled = claimGenerators.get(block.getBlockType()).handleClaimDeletion(block, claim);
        if (!wasClaimDeletionHandled) {

            claimRepository.deleteById(claimId);
        }
    }
}
