/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import uk.gov.london.ops.domain.finance.LedgerStatus;
import uk.gov.london.ops.domain.finance.LedgerType;
import uk.gov.london.ops.domain.project.ProjectLedgerEntry;
import uk.gov.london.ops.domain.project.QProjectLedgerEntry;
import uk.gov.london.ops.domain.project.SAPMetaData;
import uk.gov.london.ops.domain.project.SpendType;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Spring JPA data repository for Project data.
 */
public interface ProjectLedgerRepository extends JpaRepository<ProjectLedgerEntry, Integer>, QueryDslPredicateExecutor<ProjectLedgerEntry> {

    List<ProjectLedgerEntry> findAllByBlockId(Integer blockId);

    List<ProjectLedgerEntry> findAllByBlockIdAndYearMonthAndCategoryId(int blockId, int yearMonth, Integer categoryId);

    ProjectLedgerEntry findFirstByBlockIdAndYearMonthAndCategoryIdAndLedgerStatus(int blockId, int yearMonth, Integer categoryId, LedgerStatus ledgerStatus);

    List<ProjectLedgerEntry> findAllByBlockIdAndYearMonthAndCategoryIdAndSpendTypeAndLedgerStatus(int blockId, int yearMonth, Integer categoryId, SpendType spendType, LedgerStatus ledgerStatus);

    @Query("select  p from uk.gov.london.ops.domain.project.ProjectLedgerEntry p where p.blockId = ?1 and p.yearMonth >= (?2 * 100 + 4) and p.yearMonth < ((?2 +1) * 100 + 4) and p.ledgerType = ?3")
    List<ProjectLedgerEntry> findAllByBlockIdAndFinancialYearAndLedgerType(Integer blockId, Integer year, LedgerType ledgerType);

    @Query("select  p from uk.gov.london.ops.domain.project.ProjectLedgerEntry p where p.blockId = ?1 and p.yearMonth >= (?2 * 100 + 4) and p.yearMonth < ((?2 +1) * 100 + 4) ")
    List<ProjectLedgerEntry> findAllByBlockIdAndFinancialYear(Integer blockId, Integer year);

    @Query("select  p from uk.gov.london.ops.domain.project.ProjectLedgerEntry p where p.blockId = ?1 and p.yearMonth >= (?2 * 100 + 4) and p.yearMonth < ((?3 +1) * 100 + 4) ")
    List<ProjectLedgerEntry> findAllByBlockIdBetweenFinancialYears(Integer blockId, Integer from, Integer to);

    @Query("select  p from uk.gov.london.ops.domain.project.ProjectLedgerEntry p where p.blockId = ?1 and ((p.yearMonth < ?3 AND p.ledgerStatus = 'ACTUAL') OR " +
            "(p.yearMonth >= ?3 AND p.ledgerStatus = 'FORECAST')) " +
            "and p.yearMonth > ?2 and p.yearMonth < ?4")
    List<ProjectLedgerEntry> findHistoricActualsAndFutureForecasts(Integer blockId, Integer yearMonthFrom, Integer yearMonthCurrent, Integer yearMonthTo);

    List<ProjectLedgerEntry> findAllByLedgerStatus(LedgerStatus status);

    List<ProjectLedgerEntry> findByBlockIdAndExternalId(Integer blockId, Integer externalId);

    List<ProjectLedgerEntry> findByProjectIdAndExternalIdAndLedgerStatusIn(Integer projectId, Integer externalId, Set<LedgerStatus> statusSet);

    List<ProjectLedgerEntry> findByProjectIdAndSubCategoryAndLedgerStatusIn(Integer projectId, String subCategory, Set<LedgerStatus> statusSet);

    List<ProjectLedgerEntry> findAllByBlockIdAndLedgerType(Integer blockId, LedgerType type);

    List<ProjectLedgerEntry> findAll();

    List<ProjectLedgerEntry> findByLedgerStatusAndLedgerTypeIn(LedgerStatus status, LedgerType [] types);

    List<ProjectLedgerEntry> findAllByReclaimOfPaymentId(Integer id);

    List<ProjectLedgerEntry> findByIdIn(Collection<Integer> ids);

    List<ProjectLedgerEntry> findByLedgerStatusIn(List<LedgerStatus> status);

    default List<ProjectLedgerEntry> findAll(String projectIdOrName,
                                             String organisationName,
                                             List<LedgerType> relevantSources,
                                             List<LedgerStatus> relevantStatuses,
                                             List<String> categories,
                                             List<String> relevantProgrammes,
                                             OffsetDateTime fromDate,
                                             OffsetDateTime toDate) {
        QProjectLedgerEntry query = new QProjectLedgerEntry();
//        if (!currentUser.isGla()) {
//            query.withOrganisations(currentUser.getOrganisationIds());
//        }
        query.andSearch(projectIdOrName, organisationName);
        query.andStatuses(relevantStatuses);
        query.andSources(relevantSources);
        query.andCategories(categories);
        query.andProgrammes(relevantProgrammes);
        query.andAuthorisedDates(fromDate, toDate);

        return (List<ProjectLedgerEntry>) findAll(query.getPredicate());
    }

    List<ProjectLedgerEntry> findAllByLedgerTypeAndAuthorisedOnBetween(LedgerType type, OffsetDateTime from, OffsetDateTime to);

    long countByProjectIdAndLedgerStatus(Integer projectId, LedgerStatus status);

    long countByProjectIdAndLedgerStatusIn(Integer projectId, Collection<LedgerStatus> statuses);

    long countByProjectIdAndLedgerTypeIn(Integer projectId, Collection<LedgerType> ledgerType);

    long countByProjectIdAndReclaimOfPaymentIdNotNull(Integer projectId);

    List<ProjectLedgerEntry> findAllByProjectId(Integer projectId);

    List<ProjectLedgerEntry> findAllByProjectIdAndCategoryAndExternalId(Integer projectId, String category, Integer extId);

    @Query(value = "select distinct year_month from project_ledger_entry p where p.block_id = ?1", nativeQuery = true)
    List<Integer> findPopulatedYearsForBlock(Integer blockId);

    @Modifying
    @Query("UPDATE uk.gov.london.ops.domain.project.ProjectLedgerEntry p SET p.managingOrganisation.id = :manOrgId WHERE p.projectId in " +
            "(select pr.id from Project pr where pr.programme.id = :programmeId)")
    int updatePaymentEntriesManagingOrgByProgramme(@Param("manOrgId") int managingOrgId, @Param("programmeId") int programmeId);

    @Query(value = "select new " +
            "uk.gov.london.ops.domain.project.SAPMetaData(p.vendorName, p.transactionDate, p.category, p.categoryId, p.sapCategoryCode, p.transactionNumber, p.value, p.ledgerSource, p.spendType) " +
            "from uk.gov.london.ops.domain.project.ProjectLedgerEntry p " +
            "where p.projectId = ?1 and p.blockId = ?2 and p.yearMonth = ?3 and p.ledgerType=?4 and p.ledgerStatus = ?5 and p.categoryId = ?6 " +
            "order by p.transactionDate")
    List<SAPMetaData> getSapMetaData(Integer projectId, Integer blockId, Integer yearMonth, LedgerType ledgerType, LedgerStatus ledgerStatus, Integer categoryId);


}
