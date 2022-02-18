package uk.gov.london.ops.project

import com.fasterxml.jackson.annotation.JsonIgnore
import uk.gov.london.ops.organisation.model.OrganisationEntity
import uk.gov.london.ops.programme.domain.Programme
import uk.gov.london.ops.project.state.StateModel
import javax.persistence.*

@Entity(name = "v_project_overview")
class ProjectOverview(

        @Id
        @Column(name = "project_id")
        override var id: Int? = null,

        @Column(name = "title")
        var title: String,

        @Enumerated(EnumType.STRING)
        var recommendation: Project.Recommendation? = null,

        @ManyToOne(cascade = [])
        @JoinColumn(name = "org_id", nullable = false)
        override var organisation: OrganisationEntity,

        @JsonIgnore
        @ManyToOne(cascade = [])
        @JoinColumn(name = "managing_organisation_id")
        override var managingOrganisation: OrganisationEntity,

        @Column(name = "status")
        override var statusName: String,

        @Column(name = "substatus")
        override var subStatusName: String? = null,

        @Column(name = "state_model")
        @Enumerated(EnumType.STRING)
        override var stateModel: StateModel,

        @Column(name = "marked_for_corporate")
        override var isMarkedForCorporate: Boolean = false,

        @Column(name = "info_message")
        var infoMessage: String?=null,

        @Column(name = "template_id")
        var templateId: Int,

        @Column(name = "programme_id")
        var programmeId: Int,

        @Column(name = "prog_name")
        var programmeName: String,

        @Column(name = "prog_status")
        @Enumerated(EnumType.STRING)
        var programmeStatus: Programme.Status,

        @Column(name = "in_assessment")
        var inAssessment: Boolean = false,

        @Column(name = "enabled")
        var enabled: Boolean = false,

        @Column(name = "approval_will_generate_reclaim")
        var approvalWillGenerateReclaimPersisted: Boolean ? = null,

        @Column(name = "approval_will_generate_payment")
        var approvalWillGeneratePaymentPersisted: Boolean ? = null,

        @Column(name = "total_grant_eligibility")
        override var totalGrantEligibility: Long? = null,

        @Column(name = "suspend_payments")
        var suspendPayments: Boolean = false

) : ProjectInterface
