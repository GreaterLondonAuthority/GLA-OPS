/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.block

import uk.gov.london.ops.framework.jpa.NonJoin
import javax.persistence.*

/**
 * Project and block data overview, lightweight version
 */
@Entity(name = "v_project_block_latest")
@NonJoin("Overview entity, does not provide join information")
class ProjectBlockOverview (

        @Id
    @Column(name = "block_id")
    var projectBlockId: Int,

        @Column(name = "project_id")
    var projectId: Int,

        @Column(name = "block_display_name")
    var blockDisplayName: String,

        @Column(name = "project_block_type")
    @Enumerated(EnumType.STRING)
    var blockType: ProjectBlockType,

        @Column(name = "block_status")
    @Enumerated(EnumType.STRING)
    var blockStatus: NamedProjectBlock.BlockStatus = NamedProjectBlock.BlockStatus.UNAPPROVED,

        @Column(name = "display_order")
    var displayOrder: Int,

        @Column(name = "version_number")
    var versionNumber: Int,

        @Column(name = "block_appears_on_status")
    var blockAppearsOnStatus: String? = null,

        @Column(name = "locked_by")
    var lockedBy: String? = null,

        @Column(name = "hidden")
    var isHidden: Boolean = false,

        @Column(name = "is_new")
    var isNew: Boolean = false,

        @Column(name = "has_updates_persisted")
    var hasUpdatesPersisted: Boolean? = null,

        @Column(name = "block_marked_complete")
    var blockMarkedComplete: Boolean? = null

)
