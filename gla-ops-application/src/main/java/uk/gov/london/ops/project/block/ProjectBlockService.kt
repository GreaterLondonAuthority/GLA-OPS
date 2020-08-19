/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.block

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.london.ops.framework.exception.ValidationException
import uk.gov.london.ops.permission.PermissionService
import uk.gov.london.ops.project.implementation.repository.ProjectBlockOverviewRepository
import uk.gov.london.ops.project.implementation.repository.ProjectBlockRepository
import uk.gov.london.ops.project.implementation.repository.ProjectOverviewRepository
import uk.gov.london.ops.user.UserService

@Service
class ProjectBlockService @Autowired constructor(val projectBlockRepository: ProjectBlockRepository,
                                                 val projectOverviewRepository: ProjectOverviewRepository,
                                                 val projectBlockOverviewRepository: ProjectBlockOverviewRepository,
                                                 val projectBlockActivityMap: ProjectBlockActivityMap,
                                                 val userService: UserService,
                                                 val permissionService: PermissionService) {

    fun getProjectBlock(blockId: Int): NamedProjectBlock {
        return projectBlockRepository.findById(blockId).orElseThrow { ValidationException("Unable to find block with id: $blockId") }
    }

    fun enrichedBlock(block: NamedProjectBlock): NamedProjectBlock {
        val currentUser = userService.currentUser()

        val projectOverview = projectOverviewRepository.findByIdForUser(block.projectId, currentUser.username)
        block.projectOverview = projectOverview

        val pacl = permissionService.getProjectAccessControlList(block.projectId)

        block.allowedActions = projectBlockActivityMap.getAllowedActionsFor(projectOverview, block, currentUser, pacl)

        if (block.blockType.dependsOn != null) {
            enrichFromDependantBlock(block, projectOverview.projectId)
        }

        return block
    }

    fun enrichFromDependantBlock(block: NamedProjectBlock, projectId: Int) {
        val otherBlockOverview = projectBlockOverviewRepository.findByProjectIdAndBlockType(projectId, block.blockType.dependsOn)

        val otherBlock: NamedProjectBlock = getProjectBlock(otherBlockOverview.projectBlockId)

        if (otherBlock.blockType.dependsOn != null) {
            throw RuntimeException("Unable to base a block on another dependant block")
        }

        block.enrichFromBlock(otherBlock)
    }

    fun getLearningGrantBlockByProjectIdVersionNumber(projectId: Int, versionNumber: Int, displayOrder: Int): NamedProjectBlock? {
         return projectBlockRepository.findLearningGrantBlockByProjectIdAndVersionNumberAndDisplayOrder(projectId, versionNumber, displayOrder)
    }

}
