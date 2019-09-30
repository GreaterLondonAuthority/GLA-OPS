/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.payment.implementation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import uk.gov.london.ops.domain.project.SAPMetaData;
import uk.gov.london.ops.domain.project.SpendType;
import uk.gov.london.ops.payment.*;

import javax.transaction.Transactional;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Spring JPA data repository for Project data.
 */
public interface ProjectLedgerRepository extends JpaRepository<ProjectLedgerEntry, Integer>, QuerydslPredicateExecutor<ProjectLedgerEntry> {

    String filter_active_categories_clause = " and ((ple.ledger_type not in ('PAYMENT', 'RECEIPT')) or (ple.ledger_type = 'PAYMENT' and fc.spend_status != 'Hidden') or (ple.ledger_type = 'RECEIPT' and fc.receipt_status != 'Hidden'))";

    List<ProjectLedgerEntry> findAllByBlockId(Integer blockId);

    List<ProjectLedgerEntry> findAllByBlockIdAndYearMonthAndCategoryId(int blockId, int yearMonth, Integer categoryId);

    ProjectLedgerEntry findFirstByBlockIdAndYearMonthAndCategoryIdAndLedgerStatus(int blockId, int yearMonth, Integer categoryId, LedgerStatus ledgerStatus);

    List<ProjectLedgerEntry> findAllByBlockIdAndYearMonthAndCategoryIdAndSpendTypeAndLedgerStatus(int blockId, int yearMonth, Integer categoryId, SpendType spendType, LedgerStatus ledgerStatus);

    @Query("select  p from uk.gov.london.ops.payment.ProjectLedgerEntry p where p.blockId = ?1 and p.yearMonth >= (?2 * 100 + 4) and p.yearMonth < ((?2 +1) * 100 + 4) and p.ledgerType = ?3")
    List<ProjectLedgerEntry> findAllByBlockIdAndFinancialYearAndLedgerType(Integer blockId, Integer year, LedgerType ledgerType);

    @Query(value = "select ple.* from project_ledger_entry ple left join finance_category fc on ple.category_id = fc.id " +
            "where ple.block_id = ?1 and ple.year_month >= (?2 * 100 + 4) and ple.year_month < ((?2 +1) * 100 + 4) " + filter_active_categories_clause, nativeQuery = true)
    List<ProjectLedgerEntry> findAllByBlockIdAndFinancialYear(Integer blockId, Integer year);

    @Query(value = "select ple.* from project_ledger_entry ple left join finance_category fc on ple.category_id = fc.id " +
            "where ple.block_id = ?1 and ple.year_month >= (?2 * 100 + 4) and ple.year_month < ((?3 +1) * 100 + 4) " + filter_active_categories_clause, nativeQuery = true)
    List<ProjectLedgerEntry> findAllByBlockIdBetweenFinancialYears(Integer blockId, Integer from, Integer to);

    @Query(value = "select ple.* from project_ledger_entry ple left join finance_category fc on ple.category_id = fc.id " +
            "where ple.block_id = ?1 and ((ple.year_month < ?3 AND ple.ledger_status = 'ACTUAL') OR " +
            "(ple.year_month >= ?3 AND ple.ledger_status = 'FORECAST')) " +
            "and ple.year_month > ?2 and ple.year_month < ?4 " + filter_active_categories_clause, nativeQuery = true)
    List<ProjectLedgerEntry> findHistoricActualsAndFutureForecasts(Integer blockId, Integer yearMonthFrom, Integer yearMonthCurrent, Integer yearMonthTo);

    List<ProjectLedgerEntry> findAllByLedgerStatus(LedgerStatus status);

    Set<ProjectLedgerEntry> findAllByBlockIdAndClaimId(Integer blockId, Integer claimId);

    List<ProjectLedgerEntry> findByBlockIdAndExternalId(Integer blockId, Integer externalId);

    List<ProjectLedgerEntry> findByProjectIdAndSubCategoryAndLedgerStatusIn(Integer projectId, String subCategory, Set<LedgerStatus> statusSet);

    List<ProjectLedgerEntry> findAllByBlockIdAndLedgerType(Integer blockId, LedgerType type);

    List<ProjectLedgerEntry> findAll();

    List<ProjectLedgerEntry> findByLedgerStatusAndLedgerTypeIn(LedgerStatus status, LedgerType [] types);

    List<ProjectLedgerEntry> findAllByReclaimOfPaymentId(Integer id);

    List<ProjectLedgerEntry> findAllByInterestForPaymentId(Integer id);

    List<ProjectLedgerEntry> findByIdIn(Collection<Integer> ids);

    default List<ProjectLedgerEntry> findAll(String projectIdOrName,
                                             String organisationName,
                                             List<PaymentSource> paymentSources,
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
        query.andPaymentSources(paymentSources);
        query.andCategories(categories);
        query.andProgrammes(relevantProgrammes);
        query.andAuthorisedDates(fromDate, toDate);

        return (List<ProjectLedgerEntry>) findAll(query.getPredicate());
    }

    List<ProjectLedgerEntry> findAllByLedgerTypeAndAuthorisedOnBetween(LedgerType type, OffsetDateTime from, OffsetDateTime to);

    long countByProjectIdAndLedgerStatus(Integer projectId, LedgerStatus status);

    long countByProjectIdAndLedgerStatusIn(Integer projectId, Collection<LedgerStatus> statuses);

    long countByProjectIdAndReclaimOfPaymentIdNotNull(Integer projectId);

    List<ProjectLedgerEntry> findAllByProjectId(Integer projectId);
    List<ProjectLedgerEntry> findAllByProjectIdAndLedgerStatusIn(Integer projectId, Collection<LedgerStatus> types);

    int countAllByProjectIdAndCategoryAndYearMonth(Integer projectId, String category, Integer yearMonth );

    List<ProjectLedgerEntry> findAllByProjectIdAndLedgerType(Integer projectId, LedgerType type);

    List<ProjectLedgerEntry> findAllByProjectIdAndCategoryAndExternalId(Integer projectId, String category, Integer extId);

    @Query(value = "select distinct year_month from project_ledger_entry p where p.block_id = ?1", nativeQuery = true)
    List<Integer> findPopulatedYearsForBlock(Integer blockId);

    @Modifying
    @Query("UPDATE uk.gov.london.ops.payment.ProjectLedgerEntry p SET p.managingOrganisation.id = :manOrgId WHERE p.projectId in " +
            "(select pr.id from Project pr where pr.programme.id = :programmeId)")
    int updatePaymentEntriesManagingOrgByProgramme(@Param("manOrgId") int managingOrgId, @Param("programmeId") int programmeId);

    @Query(value = "select new " +
            "uk.gov.london.ops.domain.project.SAPMetaData(p.vendorName, p.transactionDate, p.category, p.categoryId, p.sapCategoryCode, p.transactionNumber, p.value, p.ledgerSource, p.spendType) " +
            "from uk.gov.london.ops.payment.ProjectLedgerEntry p " +
            "where p.projectId = ?1 and p.blockId = ?2 and p.yearMonth = ?3 and p.ledgerType=?4 and p.ledgerStatus = ?5 and p.categoryId = ?6 " +
            "order by p.transactionDate")
    List<SAPMetaData> getSapMetaData(Integer projectId, Integer blockId, Integer yearMonth, LedgerType ledgerType, LedgerStatus ledgerStatus, Integer categoryId);

    @Transactional
    @Modifying
    void deleteAllByProjectId(Integer projectId);

}
