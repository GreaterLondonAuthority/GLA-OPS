/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.implementation.di;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;
import uk.gov.london.common.skills.SkillsGrantType;
import uk.gov.london.ops.framework.di.DataInitialiserModule;
import uk.gov.london.ops.framework.enums.Requirement;
import uk.gov.london.ops.framework.environment.Environment;
import uk.gov.london.ops.programme.ProgrammeBuilder;
import uk.gov.london.ops.programme.ProgrammeServiceImpl;
import uk.gov.london.ops.programme.domain.Programme;
import uk.gov.london.ops.programme.domain.ProgrammeTemplate;
import uk.gov.london.ops.project.block.ProjectBlockType;
import uk.gov.london.ops.project.implementation.repository.QuestionRepository;
import uk.gov.london.ops.project.implementation.repository.TemplateRepository;
import uk.gov.london.ops.project.milestone.Milestone;
import uk.gov.london.ops.project.state.StateModel;
import uk.gov.london.ops.project.template.QuestionService;
import uk.gov.london.ops.project.template.TemplateBuilder;
import uk.gov.london.ops.project.template.TemplateServiceImpl;
import uk.gov.london.ops.project.template.domain.AnswerOption;
import uk.gov.london.ops.project.template.domain.AnswerType;
import uk.gov.london.ops.project.template.domain.FundingClaimCategory;
import uk.gov.london.ops.project.template.domain.FundingClaimPeriod;
import uk.gov.london.ops.project.template.domain.FundingClaimsTemplateBlock;
import uk.gov.london.ops.project.template.domain.FundingTemplateBlock;
import uk.gov.london.ops.project.template.domain.IndicativeGrantTemplateBlock;
import uk.gov.london.ops.project.template.domain.IndicativeTenureConfiguration;
import uk.gov.london.ops.project.template.domain.InternalQuestionsTemplateBlock;
import uk.gov.london.ops.project.template.domain.InternalTemplateBlock;
import uk.gov.london.ops.project.template.domain.LearningGrantTemplateBlock;
import uk.gov.london.ops.project.template.domain.MilestoneTemplate;
import uk.gov.london.ops.project.template.domain.MilestonesTemplateBlock;
import uk.gov.london.ops.project.template.domain.OutputsTemplateBlock;
import uk.gov.london.ops.project.template.domain.ProcessingRoute;
import uk.gov.london.ops.project.template.domain.QuestionsBlockSection;
import uk.gov.london.ops.project.template.domain.QuestionsTemplateBlock;
import uk.gov.london.ops.project.template.domain.ReceiptsTemplateBlock;
import uk.gov.london.ops.project.template.domain.Template;
import uk.gov.london.ops.project.template.domain.TemplateBlock;
import uk.gov.london.ops.project.template.domain.TemplateQuestion;
import uk.gov.london.ops.refdata.MarketType;
import uk.gov.london.ops.refdata.OutputConfigurationGroup;
import uk.gov.london.ops.refdata.OutputConfigurationService;
import uk.gov.london.ops.refdata.RefDataServiceImpl;
import uk.gov.london.ops.refdata.TenureType;
import uk.gov.london.ops.service.CRUDServiceInterface;

import javax.transaction.Transactional;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import static uk.gov.london.ops.payment.ProjectLedgerEntry.DEFAULT_LEDGER_CE_CODE;
import static uk.gov.london.ops.project.skills.AllocationType.Community;
import static uk.gov.london.ops.project.skills.AllocationType.Delivery;
import static uk.gov.london.ops.project.skills.AllocationType.InnovationFund;
import static uk.gov.london.ops.project.skills.AllocationType.LearnerSupport;
import static uk.gov.london.ops.project.skills.AllocationType.ResponseFundStrand1;
import static uk.gov.london.ops.project.template.TemplateBuilder.AFFORDABLE_HOUSING_GRANT_AGREEMENT;
import static uk.gov.london.ops.project.template.TemplateBuilder.APPROVED_PROVIDER_ROUTE_TEMPLATE_NAME;
import static uk.gov.london.ops.project.template.TemplateBuilder.AUTO_APPROVAL_TEMPLATE_ADD_NAME;
import static uk.gov.london.ops.project.template.TemplateBuilder.AUTO_APPROVAL_TEMPLATE_NAME;
import static uk.gov.london.ops.project.template.TemplateBuilder.BOROUGH_INTERVENTION_AGREEMENT;
import static uk.gov.london.ops.project.template.TemplateBuilder.DEVELOPER_LED_ROUTE_TEMPLATE_NAME;
import static uk.gov.london.ops.project.template.TemplateBuilder.DEVELOPMENT_FACILITY_AGREEMENT;
import static uk.gov.london.ops.project.template.TemplateBuilder.Directorate;
import static uk.gov.london.ops.project.template.TemplateBuilder.HIDDEN_BLOCKS_TEMPLATE_NAME;
import static uk.gov.london.ops.project.template.TemplateBuilder.HOUSING_ZONES_GENERAL;
import static uk.gov.london.ops.project.template.TemplateBuilder.INDICATIVE_TEMPLATE_NAME;
import static uk.gov.london.ops.project.template.TemplateBuilder.LEGACY_CARE_AND_SUPPORT_TEMPLATE;
import static uk.gov.london.ops.project.template.TemplateBuilder.MHC_REVOLVING_FUND;
import static uk.gov.london.ops.project.template.TemplateBuilder.MOPAC_TEMPLATE_NAME;
import static uk.gov.london.ops.project.template.TemplateBuilder.MULTI_ASSESSMENT_TEMPLATE_NAME;
import static uk.gov.london.ops.project.template.TemplateBuilder.NEGOTIATED_ROUTE_LEGACY_TEMPLATE_NAME;
import static uk.gov.london.ops.project.template.TemplateBuilder.NEGOTIATED_ROUTE_TEMPLATE_NAME;
import static uk.gov.london.ops.project.template.TemplateBuilder.TEST_HOUSING_TEMPLATE_NAME;
import static uk.gov.london.ops.project.template.TemplateBuilder.TEST_HOUSING_TEMPLATE_WITH_MILESTONE;
import static uk.gov.london.ops.refdata.TenureType.LEGACY_SHARED_OWNERSHIP;
import static uk.gov.london.ops.user.UserBuilder.DATA_INITIALISER_USER;

/**
 * @deprecated - use a feature-aligned DataInitialiser module instead
 */
@Transactional
@Component
public class TemplateDataInitialiser implements DataInitialiserModule {

    Logger log = LoggerFactory.getLogger(getClass());


    @Override
    public String getName() {
        return null;
    }
}
