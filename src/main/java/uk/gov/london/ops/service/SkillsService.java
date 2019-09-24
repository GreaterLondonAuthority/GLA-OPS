/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.service;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.london.common.skills.FundingRecord;
import uk.gov.london.common.skills.SkillsGrantType;
import uk.gov.london.ops.Environment;
import uk.gov.london.ops.audit.AuditService;
import uk.gov.london.ops.domain.skills.SkillsFundingGroupedSummary;
import uk.gov.london.ops.domain.skills.SkillsFundingSummaryEntity;
import uk.gov.london.ops.domain.skills.SkillsPaymentProfile;
import uk.gov.london.ops.repository.SkillsFundingSummaryRepository;
import uk.gov.london.ops.repository.SkillsPaymentProfileRepository;
import uk.gov.london.ops.framework.exception.ValidationException;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;

/**
 * service for managing the skills payment profiling data. Is organised by grant type.
 */
@Service
public class SkillsService {

    Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private AuditService auditService;

    @Autowired
    private SkillsFundingSummaryRepository skillsFundingSummaryRepository;

    @Autowired
    private SkillsPaymentProfileRepository skillsPaymentProfileRepository;

    @Autowired
    private Environment environment;

    @Value("${skill.payment.profile.first.year}")
    private Integer firstYear;

    @Value("${skills.profiles.paymentDueDay}")
    private Integer paymentDueDay;


    public List<SkillsPaymentProfile> getSkillsPaymentProfiles(){
        List<SkillsPaymentProfile> profiles = skillsPaymentProfileRepository.findAll();
        updatePaymentDateAndEditableForProfiles(profiles);
        return profiles;
    }

    private void updatePaymentDateAndEditableForProfiles(List<SkillsPaymentProfile> profiles) {
        for (SkillsPaymentProfile profile : profiles){
            if (profile.getPaymentDate() == null){
                profile.setPaymentDate(getPaymentDateFor(profile));
            }
            // If the profile is used in active/submitted projects, the profile is not editable or deletable
            profile.setEditable(!skillsPaymentProfileRepository.isSkillsProfileUsedForActiveProjects(getGrantType(profile.getType().name()), profile.getYear()));
        }
    }

    @NotNull
    private String getGrantType(String type) {
        if (type.equals("AEB_LEARNER_SUPPORT")) {
            type = "AEB_PROCURED";
        }
        return type;
    }

    private OffsetDateTime getPaymentDateFor(SkillsPaymentProfile profile){
        final int PAYMENT_MONTH = 7; // AUGUST;
        int paymentMonth = PAYMENT_MONTH + profile.getPeriod() > 12 ? (PAYMENT_MONTH + profile.getPeriod()) - 12 : PAYMENT_MONTH + profile.getPeriod();
        int paymentYear = PAYMENT_MONTH + profile.getPeriod() > 12 ? profile.getYear() + 1 : profile.getYear();
        return OffsetDateTime.of(paymentYear, paymentMonth, paymentDueDay, 0,0,0,0, ZoneOffset.UTC);
    }

    public List<SkillsPaymentProfile> getSkillsPaymentProfiles(SkillsGrantType type, Integer year) {
        List<SkillsPaymentProfile> profiles = skillsPaymentProfileRepository.findByTypeAndYear(type, year);
        updatePaymentDateAndEditableForProfiles(profiles);
        return profiles;
    }

    public void updateSkillsPaymentProfile(Integer id, SkillsPaymentProfile profile) {
        SkillsPaymentProfile skillsPaymentProfile = skillsPaymentProfileRepository.findById(id).orElse(null);

        if(skillsPaymentProfileRepository.isSkillsProfileUsedForActiveProjects(getGrantType(profile.getType().name()), profile.getYear())) {
            throw new ValidationException("The skills payment profile cannot be updated because is used in active projects.");
        } else {
            skillsPaymentProfile.setPercentage(profile.getPercentage());
            if (profile.getPaymentDate() != null) {
                skillsPaymentProfile.setPaymentDate(profile.getPaymentDate());
            }
            skillsPaymentProfileRepository.save(skillsPaymentProfile);
        }

    }

    public void deleteSkillsPaymentProfileByTypeAndYear(SkillsGrantType type, Integer year) {
        List<SkillsPaymentProfile> profiles = skillsPaymentProfileRepository.findByTypeAndYear(type, year);
            for(SkillsPaymentProfile profile : profiles) {
                if(skillsPaymentProfileRepository.isSkillsProfileUsedForActiveProjects(getGrantType(profile.getType().name()), profile.getYear())) {
                    throw new ValidationException("The skills payment profile cannot be deleted because is used in active projects.");
                } else {
                  List<SkillsPaymentProfile> allProfiles = skillsPaymentProfileRepository.findByType(type);
                    if(year.equals(allProfiles.stream().max(comparing(SkillsPaymentProfile::getYear)).get().getYear())) {
                        skillsPaymentProfileRepository.delete(profile);
                    } else {
                        throw new ValidationException("The skills payment profile cannot be deleted because is not the last skills profile.");
                    }
                }
            }
    }

    public List<SkillsPaymentProfile> createOrCloneYear(SkillsGrantType type) {
        return createOrCloneYear(type, firstYear);
    }

    public List<SkillsPaymentProfile> createOrCloneYear(SkillsGrantType type, Integer academicYear) {
        List<SkillsPaymentProfile> existingProfileData = skillsPaymentProfileRepository.findByType(type);
        List<SkillsPaymentProfile> nextYearProfileData = new ArrayList<>();

        Set<SkillsPaymentProfile> latestYearData = getLatestYearData(existingProfileData);

        if (latestYearData.isEmpty()) {
            for (int i = 1; i <=12; i++) {
                nextYearProfileData.add(new SkillsPaymentProfile(type, academicYear, i, new BigDecimal(0)));
            }
        } else {
            for (SkillsPaymentProfile lastYear : latestYearData) {
                nextYearProfileData.add(new SkillsPaymentProfile(type, lastYear.getYear()  + 1, lastYear.getPeriod(), lastYear.getPercentage()));
            }
        }

        //updatePaymentDateAndEditableForProfiles(nextYearProfileData);
        return skillsPaymentProfileRepository.saveAll(nextYearProfileData);
    }

    private Set<SkillsPaymentProfile> getLatestYearData(List<SkillsPaymentProfile> existingProfileData) {
        if (existingProfileData == null || existingProfileData.isEmpty()) {
            return Collections.emptySet();
        }
        int maxYear = existingProfileData.stream().mapToInt(SkillsPaymentProfile::getYear).max().getAsInt();
        return existingProfileData.stream().filter(s -> maxYear == s.getYear()).collect(Collectors.toSet());
    }

    /**
     * only for use in test environments at the moment.
     */
    public void deleteAllExistingData() {
        if (environment.isTestEnvironment()) {
            skillsPaymentProfileRepository.deleteAll();
            skillsFundingSummaryRepository.deleteAll();
        }
    }

    public List<SkillsFundingGroupedSummary> getGroupedSummaries(Integer ukprn, Integer academicYear, SkillsGrantType grantType) {
        if (ukprn != null && academicYear != null) {
            return skillsFundingSummaryRepository.getGroupedSummariesByUkprnAndYearAndGrantType(ukprn, academicYear, grantType);
        }
        else {
            return new ArrayList<>();
        }
    }

    @Transactional
    public void handleILRFundingSummary(List<FundingRecord> fundingSummaryList, Integer academicYear, Integer period) {
        validateNoClaims(academicYear,period);
        deleteAllByAcademicYearAndPeriod(academicYear, period);
        for (FundingRecord entry: fundingSummaryList) {
            saveFundingSummary(
                    entry.getUkprn(), entry.getAcademicYear(), entry.getPeriod(), entry.getActualYear(), entry.getActualMonth(), entry.getFundingLine(), entry.getSource(),
                    entry.getCategory(), entry.getGrantType(), entry.getTotalPayment());
        }

        auditService.auditCurrentUserActivity("received "+fundingSummaryList.size()+" ILR data entries");
    }

    public void validateNoClaims(Integer academicYear, Integer period) {
        Integer numberOfClaimsForEntity = skillsFundingSummaryRepository.getNumberOfClaimsForEntity(academicYear,period);
        if (numberOfClaimsForEntity > 0){
            log.error(String.format("Failed to override data for period %d, academic year %d as claim exists", period, academicYear));
            throw new ValidationException("Cannot override data for specified period as a claim already exists");
        }
    }

    private void deleteAllByAcademicYearAndPeriod(Integer adacademicYear, Integer period) {
        skillsFundingSummaryRepository.deleteAllByAcademicYearAndPeriod(adacademicYear, period);

    }

    public void saveFundingSummary(Integer ukprn, Integer academicYear, Integer period, Integer actualYear, Integer actualMonth, String fundingLine, String source, String category, SkillsGrantType grantType, BigDecimal totalPayment) {
        SkillsFundingSummaryEntity entity = new SkillsFundingSummaryEntity(ukprn, academicYear, period, actualYear, actualMonth, fundingLine, source, category, grantType, totalPayment);
        skillsFundingSummaryRepository.save(entity);

    }

    public Set<SkillsFundingSummaryEntity> findAllMatching(Integer ukprn, SkillsGrantType grantType) {
        return skillsFundingSummaryRepository.findAllByUkprnAndGrantType(ukprn, grantType);
    }

//    public BigDecimal getCalculatedTotalForYearMonthAndMatchRule(Integer ukprn, Integer academicYear, Integer period, Set<String> toMatch, Set<String> toNotMatch) {
//        if ((toMatch != null && toMatch.size() > 1) || (toNotMatch != null && toNotMatch.size() > 1)) {
//            throw new ValidationException("Unable to handle multiple matching rules at the moment, please adjust template.");
//        }
//
//        if (toMatch != null && !toMatch.isEmpty()) {
//            return skillsFundingSummaryRepository.getCalculatedTotalForYearMonthAndMatchRule(ukprn, academicYear, period, toMatch.iterator().next());
//        }
//        if (toNotMatch != null && !toNotMatch.isEmpty()) {
//            return skillsFundingSummaryRepository.getCalculatedTotalForYearMonthAndNotMatchRule(ukprn, academicYear, period, toNotMatch.iterator().next());
//        }
//        return BigDecimal.ZERO;
//    }
}
