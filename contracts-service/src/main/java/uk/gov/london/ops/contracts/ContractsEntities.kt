/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.contracts

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.london.ops.framework.enums.ContractWorkflowType
import javax.persistence.*

@Entity(name="v_contracts_summary")
class ContractTemplatesEntity (

    @Id
    var id: Int? = null,

    @Column
    val name: String? = null,

    @Column
    @Enumerated(EnumType.STRING)
    val contractWorkflowType: ContractWorkflowType? = null,

    @Column
    val templates: String? = null,


) {

    fun getTemplateList(): List<String> {
        return templates?.split(",") ?: listOf()
    }
}

internal interface ContractTemplatesEntityRepository : JpaRepository<ContractTemplatesEntity?, Int?>
