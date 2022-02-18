/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.implementation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uk.gov.london.ops.organisation.model.OrganisationEntity;
import uk.gov.london.ops.programme.domain.Programme;
import uk.gov.london.ops.project.Project;
import uk.gov.london.ops.project.ProjectContactSummary;
import uk.gov.london.ops.project.ProjectModel;
import uk.gov.london.ops.project.block.ProjectBlockHistoryItem;
import uk.gov.london.ops.project.template.domain.Template;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Spring JPA data repository for Project data.
 */
public interface ProjectRepository extends JpaRepository<Project, Integer> {

    @Query(value = "select  * from Project p inner join v_project_block_active active "
           + "on active.project_id = p.id and active.block_type = 'DETAILS' "
           + "INNER JOIN project_details_block pdb on active.id = pdb.id "
           + "where pdb.pcs_project_code = ?1 limit 1", nativeQuery = true)
    Project findFirstByLegacyProjectCode(Integer legacyProjectCode);

    @Query(value = "select  * from Project p inner join v_project_block_active active "
           + "on active.project_id = p.id and active.project_block_type = ?1 "
           + "INNER JOIN wbs_code wbs on active.id = wbs.block_id "
           + "where wbs.code = ?2 ", nativeQuery = true)
    Set<Project> findAllByPaymentsWBSCode(String blockType, String wbsCode);

    @Query(value = "select p.id from Project p inner join v_project_block_active active "
           + "on active.project_id = p.id and active.project_block_type in ('ProjectBudgets', 'Receipts') "
           + "INNER JOIN wbs_code wbs on active.id = wbs.block_id "
           + "where wbs.code = ?1 ", nativeQuery = true)
    Set<Integer> findAllProjectIdsByWBSCode(String wbsCode);

    List<Project> findAllByOrderByLastModifiedDesc();

    @Query(value = "select * from Project p inner join v_project_block_active active "
           + "on active.project_id = p.id and active.block_type = 'DETAILS' "
           + "INNER JOIN project_details_block pdb on active.id = pdb.id "
           + "where pdb.title = ?1 ", nativeQuery = true)
    List<Project> findAllByTitle(String title);

    @Query(value = "select p.id from Project p inner join v_project_block_active active "
           + "on active.project_id = p.id and active.block_type = 'DETAILS' "
           + "INNER JOIN project_details_block pdb on active.id = pdb.id "
           + "where pdb.title like concat('%', ?1, '%')", nativeQuery = true)
    List<Integer> findAllProjectIdsByTitleLike(String title);

    List<Project> findAllByOrganisation(OrganisationEntity organisation);

    List<Project> findAllByOrganisationAndStatusName(OrganisationEntity organisation, String statusName);

    List<Project> findAllByOrganisationId(Integer organisationId);

    /**
     * @return a list of organisations which have created or are developers of projects within the given organisation group.
     */
    @Query(value = "SELECT * FROM Project p inner join v_project_block_active active "
           + "on active.project_id = p.id and active.block_type = 'DETAILS' "
           + "INNER JOIN project_details_block pdb on active.id = pdb.id "
           + "where p.organisation_group_id = ?1 and (p.org_id = ?2 or pdb.developing_organisation_id = ?2)", nativeQuery = true)
    List<Project> findAllByGroupAndOrganisation(Integer organisationGroupId, Integer organisationId);

    List<Project> findAllByProgramme(Programme programme);

    int countAllByProgramme(Programme programme);

    List<Project> findAllByProgrammeAndTemplate(Programme programme, Template template);

    List<Project> findAllByProgrammeAndTemplateAndOrganisationId(Programme programme,
                                                                 Template template,
                                                                 Integer organisationId);

    List<Project> findAllByTemplate(Template template);

    List<Project> findAllByTemplateAndStatusName(Template template, String status);

    @Query(value = "SELECT id FROM project p where p.programme_id = ?1 and p.template_id = ?2", nativeQuery = true)
    Integer[] findAllIdByProgrammeIdAndTemplateId(Integer programmeId, Integer templateId);

    @Query(value = "SELECT id FROM project p where p.template_id = ?1", nativeQuery = true)
    Integer[] findAllIdByTemplateId(Integer templateId);

    @Query(value = "select new  uk.gov.london.ops.project.ProjectModel(p.id, p.statusName) "
                   + "from Project p where p.template.id = ?1")
    List<ProjectModel> findAllProjectModelByTemplateId(Integer templateId);

    Integer countByProgrammeAndStatusName(Programme programme, String statusName);

    @Query(value = "SELECT * FROM project p "
            + "inner join project_block pb on p.id  = pb.project_id and pb.project_block_type = ?1 "
            + "and pb.block_status = 'LAST_APPROVED' "
            + "inner join learning_grant_block lgb on lgb.id  = pb.id and lgb.grant_type = 'AEB_GRANT' "
            + "inner join learning_grant_entry lge on lge.block_id = pb.id "
            + "where  p.status= 'Active' and TO_CHAR(lge.payment_date, 'DD/MM/YYYY') = ?2 ", nativeQuery = true)
    List<Project> findAllProjectsWithScheduledPaymentDue(String blockType, String date);

    @Query(value = "SELECT count(*) FROM Project p inner join v_project_block_active active "
           + "on active.project_id = p.id and active.block_type = 'GRANT_SOURCE' "
           + "INNER JOIN grant_source_block gsb on active.id = gsb.id "
           + "where p.programme_id = ?1 and gsb.associated_project = true", nativeQuery = true)
    Integer countAssociatedProjects(Integer programmeId);

    @Modifying
    @Query("UPDATE Project p SET p.managingOrganisation.id = :manOrgId WHERE p.programme.id = :programmeId")
    int updateProjectManagingOrgByProgramme(@Param("manOrgId") int managingOrgId, @Param("programmeId") int programmeId);

    @Query(value = "select * from project p inner join project_block pb on p.id=pb.project_id "
            + "inner join PROJECT_BLOCK_QUESTION pbq on pb.id = pbq.project_block_id "
            + "inner join template_question tq on tq.id = pbq.question_id where tq.question_id = ?1", nativeQuery = true)
    List<Project> findAllForQuestion(Integer questionId);

    @Query(value = "select * from project p "
            + " where p.status='Active' and p.subStatus='UnapprovedChanges' and p.template_id = ?1 "
            + " and p.last_modified <= ?2 and p.last_modified >= ?3", nativeQuery = true)
    List<Project> findAllProjectNotSubmitted(Integer templateId, OffsetDateTime startDateTime, OffsetDateTime endDateTime);

    @Modifying
    @Query(value = "update funding_block set start_year = :startYear, year_available_to = :numberOfYears where id in ("
            + "select fb.id from funding_block fb "
            + "inner join project_block pb on fb.id = pb.id "
            + "inner join project p on pb.project_id = p.id "
            + "where programme_id = :programmeId)", nativeQuery = true)
    int updateBudgetBlockStartEndYear(@Param("startYear") int startYear, @Param("numberOfYears") int numberOfYears,
                                      @Param("programmeId") int programmeId);

    @Modifying
    @Query(value = "update funding_block set start_year = :startYear, year_available_to = :numberOfYears where id in ("
            + "select fb.id from funding_block fb "
            + "inner join project_block pb on fb.id = pb.id "
            + "inner join project p on pb.project_id = p.id "
            + "where programme_id = :programmeId and template_id = :templateId)", nativeQuery = true)
    int resetBudgetBlockStartEndYear(@Param("startYear") int startYear, @Param("numberOfYears") int numberOfYears,
                                     @Param("programmeId") int programmeId, @Param("templateId") int templateId);


    @Query(value = "select count(*) from project p inner join project_block pb on p.id=pb.project_id "
           + "inner join PROJECT_BLOCK_QUESTION pbq on pb.id = pbq.project_block_id "
           + "inner join template_question tq on tq.id = pbq.question_id where tq.question_id = ?1", nativeQuery = true)
    Integer countByQuestion(Integer questionId);

    @Query(value = "select case when count(1) > 0 then true else false end as permitted "
           + " from v_project_permissions "
           + " where  username = ?1 and project_id = ?2 ", nativeQuery = true)
    Boolean checkAccessForProject(String username, Integer projectId);

    @Query(value = "select substring(name, 6, 50) from user_roles ur "
        + "inner join project_access_control pac on pac.organisation_id = ur.organisation_id and pac.project_id = ?2 "
        + "where username = ?1 and approved = true", nativeQuery = true)
    List<String> getUserRolesForProject(String username, Integer projectId);

    @Query(value = "select new uk.gov.london.ops.project.block.ProjectBlockHistoryItem(p.id, pb.id, pb.blockStatus, "
            + "pb.versionNumber, pb.lastModified, pb.modifiedBy, pb.approverUsername, pb.approvedOnStatus) "
            + "from Project p join p.projectBlocks pb where p.id = ?1 and pb.displayOrder = ?2 order by pb.versionNumber")
    List<ProjectBlockHistoryItem> getProjectHistoryForProjectAndDisplayOrder(Integer projectId, Integer displayOrder);

    @Query(value = "select case when count(1) > 0 then true else false end as used "
        + " from internal_risk_block where rating_id = ?1 ", nativeQuery = true)
    Boolean isRiskRatingUsedForProject(Integer riskRatingId);

    Integer countByTemplateAndOrganisationAndStatusNameIsNot(Template template, OrganisationEntity organisation, String statusName);

    @Query(value = "select count(1) > 0 from project_history ph "
            + "where ph.project_id = ?1 and (status = 'Submitted' or substatus = 'Submitted')", nativeQuery = true)
    boolean hasProjectBeenSubmittedPreviously(Integer projectId);

    @Query(value = "select count(1) > 0 from project_history ph "
        + "where ph.project_id = ?1 and transition = 'Returned'", nativeQuery = true)
    boolean hasProjectBeenReturnedPreviously(Integer projectId);

    @Modifying
    @Query(value = "update Project set lastModified = ?1 where id = ?2")
    void updateLastModifiedForProject(OffsetDateTime time, Integer projectId);

    @Query(value = "select p.id from project p inner join project_block pb on p.id = pb.project_id "
            + "where pb.project_block_type = ?1", nativeQuery = true)
    Set<Integer> getProjectIdsContainingBlock(String type);

    @Query(value = "select pdb.sap_id "
            + "from project_block pb  "
            + "join project_details_block pdb on pb.id = pdb.id "
            + "where  pb.project_id =?1 and pb.block_type = 'DETAILS' and latest_for_project is not null ", nativeQuery = true)
    String getSapIdFromProjectDetails(Integer projectId);

    @Query(value = "select distinct pdb.sap_id "
            + "from project p "
            + "join project_block pb on p.id = pb.project_id "
            + "join project_details_block pdb on pb.id = pdb.id "
            + "where  p.org_id =?1 and pb.block_type = 'DETAILS'  ", nativeQuery = true)
    Set<String> getAllSapIdUsedForOrganisation(Integer organisationId);

    @Query(value = "select new uk.gov.london.ops.project.ProjectContactSummary("
            + "p.id, pdb.title, pdb.mainContactEmail,  pdb.mainContact, "
            + "o.id, o.name) "
            + "from uk.gov.london.ops.project.Project p "
            + "join p.projectBlocks pb  "
            + "join p.organisation o "
            + "join uk.gov.london.ops.project.block.ProjectDetailsBlock pdb on p.id = pdb.project.id "
            + "where pb.blockType = 'Details' and pb.latestVersion = true "
            + "and p.programme.id = ?1 "
            + "and p.template.id in ?2 "
            + "and p.statusName= ?3")
    Set<ProjectContactSummary> findOwnerDetailsBy(Integer programmeId, Set<Integer> templateIds, String projectStatus);

    @Query(value = "select new uk.gov.london.ops.project.ProjectContactSummary("
            + "p.id, pdb.title, pdb.secondaryContactEmail,  pdb.secondaryContact, "
            + "o.id, o.name) "
            + "from uk.gov.london.ops.project.Project p "
            + "join p.projectBlocks pb  "
            + "join p.organisation o "
            + "join uk.gov.london.ops.project.block.ProjectDetailsBlock pdb on p.id = pdb.project.id "
            + "where pb.blockType = 'Details' and pb.latestVersion = true "
            + "and p.programme.id = ?1 "
            + "and p.template.id in ?2 "
            + "and p.statusName= ?3")
    Set<ProjectContactSummary> findSecondaryContactDetailsBy(Integer programmeId, Set<Integer> templateIds, String projectStatus);


    @Query(value = "select new uk.gov.london.ops.project.ProjectContactSummary("
            + "p.id, pdb.title, u.username,  CONCAT(u.firstName, ' ', u.lastName), "
            + "o.id, o.name) "
            + "from uk.gov.london.ops.project.Project p "
            + "join p.projectBlocks pb  "
            + "join p.organisation o "
            + "join uk.gov.london.ops.role.model.Role r on o.id = r.organisation.id "
            + "join uk.gov.london.ops.user.domain.UserEntity u on r.username = u.username "
            + "join uk.gov.london.ops.project.block.ProjectDetailsBlock pdb on p.id = pdb.project.id "
            + "where pb.blockType = 'Details' and pb.latestVersion = true "
            + "and p.programme.id = ?1 "
            + "and p.template.id in ?2 "
            + "and p.statusName= ?3 "
            + "and r.name in ?4")
    Set<ProjectContactSummary> findOrganisationAdminsDetailsBy(Integer programmeId, Set<Integer> templateIds, String projectStatus, Collection<String> roles);

    @Query(value = "select suspend_payments from project where id = ?1", nativeQuery = true)
    boolean isPaymentsSuspended(Integer projectId);

}
