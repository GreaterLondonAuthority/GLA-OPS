import {downgradeComponent, downgradeInjectable, getAngularJSGlobal} from "@angular/upgrade/static";
import {TemplateBlockMilestonesComponent} from "./template-configuration/template-block-milestones/template-block-milestones.component";
import {DeleteButtonComponent} from "./shared/delete-button/delete-button.component";
import {ToggleIconComponent} from "./shared/toggle-icon/toggle-icon.component";
import {FeatureToggleService} from "./feature-toggle/feature-toggle.service";
import {ConfirmationDialogService} from "./shared/confirmation-dialog/confirmation-dialog.service";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {ReferenceDataService} from "./reference-data/reference-data.service";
import {YesNoInputComponent} from "./shared/yes-no-input/yes-no-input.component";
import {TeamsDefaultAccessListComponent} from "./teams-default-access-list/teams-default-access-list.component";
import {ShowUpDownArrowButtonsComponent} from "./shared/show-up-down-arrow-buttons/show-up-down-arrow-buttons.component";
import {InfoTooltipComponent} from "./shared/info-tooltip/info-tooltip.component";
import {MarkdownComponent} from "./shared/markdown/markdown.component";
import {PageHeaderComponent} from "./shared/page-header/page-header.component";
import {ProjectHeaderComponent} from "./shared/project-header/project-header.component";
import {JsonViewerComponent} from "./template-configuration/json-viewer/json-viewer.component";
import {Title} from "@angular/platform-browser";
import {NavigationService} from "./navigation/navigation.service";
import {AuthService} from "./auth/auth.service";
import {SpinnerComponent} from "./shared/spinner/spinner.component";
import {IconNewComponent} from "./shared/icon-new/icon-new.component";
import {RemainingCharactersComponent} from "./shared/remaining-characters/remaining-characters.component";
import {MultiSelectComponent} from "./shared/multi-select/multi-select.component";
import {OutputCategoriesComponent} from './categories/output-categories/output-categories/output-categories.component';
import {OutputCategoryModalComponent} from './categories/output-categories/output-category-modal/output-category-modal.component';
import {PasswordExpiredComponent} from "./password-expired/password-expired.component";
import {WellComponent} from "./shared/well/well.component";
import {MultiPanelComponent} from "./shared/multi-panel/multi-panel.component";
import {PaginationComponent} from "./shared/pagination/pagination.component";
import {ContractTypesComponent} from "./contract-types/contract-types.component";
import {TileComponent} from "./shared/tile/tile.component";
import {OrganisationSapIdModalService} from "./organisation/organisation-sap-id-modal/organisation-sap-id-modal.service";
import {NgxPermissionsService} from "ngx-permissions";
import {UserService} from "./user/user.service";
import {ConfirmationComponent} from "./shared/confirmation/confirmation.component";
import {TopMenuComponent} from "./top-menu/top-menu.component";
import {SessionService} from "./session/session.service";
import {BannerMessageComponent} from "./banner-message/banner-message.component";
import {InternalProjectAdminBlockComponent} from "./project-block/internal-project-admin-block/internal-project-admin-block.component";
import {ToastrUtilService} from "./shared/toastr/toastr-util.service";
import {ErrorService} from "./shared/error/error.service";
import {ProjectBlockService} from "./project-block/project-block.service";
import {OutputCategoryService} from "./categories/output-categories/output-category.service";
import {TemplateService} from "./template/template.service";
import {DesignStandardsBlockComponent} from "./project-block/design-standards-block/design-standards-block.component";
import {LoadingMaskService} from "./shared/loading-mask/loading-mask.service";
import {LoadingMaskComponent} from "./shared/loading-mask/loading-mask.component";
import {LoginPageComponent} from "./login-page/login-page.component";
import {ConfigurationService} from "./configuration/configuration.service";
import {FooterComponent} from "./footer/footer.component";
import {HeaderComponent} from "./header/header.component";
import {MetadataService} from "./metadata/metadata.service";
import {CookieWarningComponent} from "./cookie-warning/cookie-warning.component";
import {TopBarComponent} from "./top-bar/top-bar.component";
import {AppComponent} from "./app.component";
import {TemplateInternalBlockComponent} from "./template-configuration/template-internal-block/template-internal-block.component";
import {TemplateInternalBlocksComponent} from "./template-configuration/template-internal-blocks/template-internal-blocks.component";
import {TemplateExternalBlockComponent} from "./template-configuration/template-external-block/template-external-block.component";
import {TemplateExternalBlocksComponent} from "./template-configuration/template-external-blocks/template-external-blocks.component";
import {TemplateBlockOtherFundingComponent} from "./template-configuration/template-block-other-funding/template-block-other-funding.component";
import {TemplateBlockFundingComponent} from "./template-configuration/template-block-funding/template-block-funding.component";
import {TemplateBlockStartsAndCompletionsComponent} from "./template-configuration/template-block-starts-and-completions/template-block-starts-and-completions.component";
import {TemplateBlockGrantSourceComponent} from "./template-configuration/template-block-grant-source/template-block-grant-source.component";
import {RegistrationTypePageComponent} from "./registration-type-page/registration-type-page.component";
import {RequestPasswordResetPageComponent} from "./request-password-reset-page/request-password-reset-page.component";
import {AffordableHomesBlockComponent} from "./project-block/affordable-homes-block/affordable-homes-block.component";
import {SectionHeaderComponent} from "./shared/section-header/section-header.component";
import {QuarterlyBudgetTableComponent} from "./quarterly-budget-table/quarterly-budget-table.component";
import {ProjectFundingService} from "./funding/project-funding.service";
import {TemplateBlockDeliveryPartnersComponent} from './template-configuration/template-block-delivery-partners/template-block-delivery-partners.component';
import {AffordableHomesSummaryReportComponent} from "./summary-report/affordable-homes-summary-report/affordable-homes-summary-report.component";
import {AffordableHomesChangeReportComponent} from "./change-report/affordable-homes-change-report/affordable-homes-change-report.component";
import {ReportService} from "./change-report/report.service";
import {ChangeReportStaticTextComponent} from "./change-report/change-report-static-text/change-report-static-text.component";
import {ChangeReportTableComponent} from "./change-report/change-report-table/change-report-table.component";
import {ChangeReportTableSeparatorComponent} from "./change-report/change-report-table/change-report-table-separator/change-report-table-separator.component";
import {CheckboxFilterComponent} from "./shared/checkbox-filter/checkbox-filter.component";
import {SearchFieldComponent} from "./shared/search-field/search-field.component";
import {MobileDeviceWarningComponent} from "./shared/mobile-device-warning/mobile-device-warning.component";
import {BroadcastsComponent} from "./broadcasts/broadcasts.component";
import {BroadcastComponent} from "./broadcasts/broadcast/broadcast.component";
import {BroadcastService} from "./broadcasts/broadcast.service";
import {ProjectService} from "./project/project.service";
import {NewProjectPageComponent} from "./project/new-project-page/new-project-page.component";
import {CategoriesComponent} from './categories/categories.component';
import {FinanceCategoriesComponent} from './categories/finance-categories/finance-categories.component';
import {FinanceCategoryModalComponent} from './categories/finance-categories/finance-category-modal/finance-category-modal.component';
import {FinanceService} from "./categories/finance-categories/finance-service.service";
import {TransitionService} from "./project/transition.service";
import {TemplateBlockLearningGrantComponent} from "./template-configuration/template-block-learning-grant/template-block-learning-grant.component"
import {BlockUsageComponent} from "./admin/block-usage/block-usage.component";
import {EmailReportsComponent} from "./admin/email-reports/email-reports.component";
import {ProjectsPageComponent} from "./project/projects-page/projects-page.component";
import {ClaimModalComponent} from "./claim-modal/claim-modal.component";
import {ActualsMetadataModalComponent} from './actuals-metadata-modal/actuals-metadata-modal.component'
import {ProjectOverviewPageComponent} from "./project/project-overview-page/project-overview-page.component";
import {HeaderStatusComponent} from "./shared/header-status/header-status.component";
import {OrganisationRequestAccessModalComponent} from './organisation-request-access-modal/organisation-request-access-modal.component';
import {ParentQuestionModalComponent} from "./template-configuration/template-block-questions/parent-question-modal/parent-question-modal.component";
import {ContractDetailsComponent} from "./organisation/contracts/contract-details/contract-details.component";
import {FileUploadComponent} from "./file-upload/file-upload.component";
import {FileUploadButtonComponent} from "./file-upload/file-upload-button/file-upload-button.component";
import {DeleteFileModalComponent} from "./file-upload/delete-file-modal/delete-file-modal.component";
import {ErrorModalComponent} from "./shared/error/error-modal/error-modal.component";
import {ContractVariationComponent} from "./organisation/contracts/contract-variation/contract-variation.component";
import {ActionDropdownComponent} from './shared/action-dropdown/action-dropdown.component';
import {QuestionsPageComponent} from "./questions/questions-page/questions-page.component";
import {QuestionsComponent} from './questions/questions/questions.component';
import {DateInputComponent} from './shared/date-input/date-input.component';
import {TeamMembersComponent} from "./organisation/team-members/team-members.component";
import { FundingActivitiesCancelModalComponent } from './funding/funding-activities-cancel-modal/funding-activities-cancel-modal.component';
import {TemplateBlockQuestionsComponent} from "./template-configuration/template-block-questions/template-block-questions.component";
import { FundingQuarterCancelModalComponent } from './funding/funding-quarter-cancel-modal/funding-quarter-cancel-modal.component';
import { TemplateBlockUserDefinedOutputsComponent } from './template-configuration/template-block-user-defined-outputs/template-block-user-defined-outputs.component';
import { TemplateBlockProjectElementsComponent } from './template-configuration/template-block-project-elements/template-block-project-elements.component';
import { TemplateBlockProjectObjectivesComponent } from './template-configuration/template-block-project-objectives/template-block-project-objectives.component';
import { UserPasswordResetModalService } from './user-password-reset-modal/user-password-reset-modal.service';
import { TemplateBlockOutputsComponent } from './template-configuration/template-block-outputs/template-block-outputs.component';
import { TemplateBlockDetailsComponent } from './template-configuration/template-block-details/template-block-details.component';
import { ProgrammeAllocationsPageComponent } from './project/programme-allocations-page/programme-allocations-page.component';


export class Downgrades {
  downgrade() {
    const ng1Gla = getAngularJSGlobal().module('GLA');

    //Components
    ng1Gla.directive('deleteButton', downgradeComponent({component: DeleteButtonComponent}));
    ng1Gla.directive('glaTemplateBlockMilestones', downgradeComponent({component: TemplateBlockMilestonesComponent}));
    ng1Gla.directive('glaTemplateBlockOtherFunding', downgradeComponent({component: TemplateBlockOtherFundingComponent}));
    ng1Gla.directive('toggleIcon', downgradeComponent({component: ToggleIconComponent}));
    ng1Gla.directive('glaYesNoInput', downgradeComponent({component: YesNoInputComponent}));
    ng1Gla.directive('glaTeamsDefaultAccessList', downgradeComponent({component: TeamsDefaultAccessListComponent}));
    ng1Gla.directive('showUpDownArrowButtons', downgradeComponent({component: ShowUpDownArrowButtonsComponent}));
    ng1Gla.directive('glaInfoTooltip', downgradeComponent({component: InfoTooltipComponent}));
    ng1Gla.directive('glaMarkdown', downgradeComponent({component: MarkdownComponent}));
    ng1Gla.directive('glaPageHeader', downgradeComponent({component: PageHeaderComponent}));
    ng1Gla.directive('glaProjectHeader', downgradeComponent({component: ProjectHeaderComponent}));
    ng1Gla.directive('glaJsonViewer', downgradeComponent({component: JsonViewerComponent}));
    ng1Gla.directive('glaTemplateInternalBlock', downgradeComponent({component: TemplateInternalBlockComponent}));
    ng1Gla.directive('glaTemplateInternalBlocks', downgradeComponent({component: TemplateInternalBlocksComponent}));
    ng1Gla.directive('glaTemplateExternalBlock', downgradeComponent({component: TemplateExternalBlockComponent}));
    ng1Gla.directive('glaTemplateExternalBlocks', downgradeComponent({component: TemplateExternalBlocksComponent}));
    ng1Gla.directive('glaSpinner', downgradeComponent({component: SpinnerComponent}));
    ng1Gla.directive('glaIconNew', downgradeComponent({component: IconNewComponent}));
    ng1Gla.directive('glaRemainingCharacters', downgradeComponent({component: RemainingCharactersComponent}));
    ng1Gla.directive('glaMultiSelect', downgradeComponent({component: MultiSelectComponent}));
    ng1Gla.directive('glaWell', downgradeComponent({component: WellComponent}));
    ng1Gla.directive('glaMultiPanel', downgradeComponent({component: MultiPanelComponent}));
    ng1Gla.directive('glaPagination', downgradeComponent({component: PaginationComponent}));
    ng1Gla.directive('glaPasswordExpired', downgradeComponent({component: PasswordExpiredComponent}));
    ng1Gla.directive('glaOutputCategories', downgradeComponent({component: OutputCategoriesComponent}));
    ng1Gla.directive('glaOutputCategoryModal', downgradeComponent({component: OutputCategoryModalComponent}));
    ng1Gla.directive('glaTile', downgradeComponent({component: TileComponent}));
    ng1Gla.directive('glaConfirmation', downgradeComponent({component: ConfirmationComponent}));
    ng1Gla.directive('glaTopMenu', downgradeComponent({component: TopMenuComponent}));
    ng1Gla.directive('glaTopBar', downgradeComponent({component: TopBarComponent}));
    ng1Gla.directive('glaBannerMessage', downgradeComponent({component: BannerMessageComponent}));
    ng1Gla.directive('glaLoadingMask', downgradeComponent({component: LoadingMaskComponent}));
    ng1Gla.directive('internalProjectAdminBlock', downgradeComponent({component: InternalProjectAdminBlockComponent}));
    ng1Gla.directive('glaDesignStandardsBlock', downgradeComponent({component: DesignStandardsBlockComponent}));
    ng1Gla.directive('glaLoginPage', downgradeComponent({component: LoginPageComponent}));
    ng1Gla.directive('glaFooter', downgradeComponent({component: FooterComponent}));
    ng1Gla.directive('glaHeader', downgradeComponent({component: HeaderComponent}));
    ng1Gla.directive('glaCookieWarning', downgradeComponent({component: CookieWarningComponent}));
    ng1Gla.directive('glaAppRoot', downgradeComponent({component: AppComponent}));
    ng1Gla.directive('glaRegistrationTypePage', downgradeComponent({component: RegistrationTypePageComponent}));
    ng1Gla.directive('glaRequestPasswordResetPage', downgradeComponent({component: RequestPasswordResetPageComponent}));
    ng1Gla.directive('glaIndicativeStartsAndCompletionsBlock', downgradeComponent({component: AffordableHomesBlockComponent}));
    ng1Gla.directive('glaSectionHeader', downgradeComponent({component: SectionHeaderComponent}));
    ng1Gla.directive('glaTemplateBlockGrantSource', downgradeComponent({component: TemplateBlockGrantSourceComponent}));
    ng1Gla.directive('glaQuarterlyBudgetTable', downgradeComponent({component: QuarterlyBudgetTableComponent}));
    ng1Gla.directive('glaTemplateBlockDeliveryPartners', downgradeComponent({component: TemplateBlockDeliveryPartnersComponent}));
    ng1Gla.directive('glaIndicativeStartsAndCompletionsSummaryReport', downgradeComponent({component: AffordableHomesSummaryReportComponent}));
    ng1Gla.directive('glaIndicativeStartsAndCompletionsChangeReport', downgradeComponent({component: AffordableHomesChangeReportComponent}));
    ng1Gla.directive('glaChangeReportStaticText', downgradeComponent({component: ChangeReportStaticTextComponent}));
    ng1Gla.directive('glaChangeReportTable', downgradeComponent({component: ChangeReportTableComponent}));
    ng1Gla.directive('glaChangeReportTableSeparator', downgradeComponent({component: ChangeReportTableSeparatorComponent}));
    ng1Gla.directive('glaCheckboxFilter', downgradeComponent({component: CheckboxFilterComponent}));
    ng1Gla.directive('glaSearchField', downgradeComponent({component: SearchFieldComponent}));
    ng1Gla.directive('glaMobileDeviceWarning', downgradeComponent({component: MobileDeviceWarningComponent}));
    ng1Gla.directive('glaBroadcasts', downgradeComponent({component: BroadcastsComponent}));
    ng1Gla.directive('glaBroadcast', downgradeComponent({component: BroadcastComponent}));
    ng1Gla.directive('glaTemplateBlockFunding', downgradeComponent({component: TemplateBlockFundingComponent}));
    ng1Gla.directive('glaTemplateBlockStartsAndCompletions', downgradeComponent({component: TemplateBlockStartsAndCompletionsComponent}));
    ng1Gla.directive('glaNewProjectPage', downgradeComponent({component: NewProjectPageComponent}));
    ng1Gla.directive('glaCategories', downgradeComponent({component: CategoriesComponent}));
    ng1Gla.directive('glaFinanceCategories', downgradeComponent({component: FinanceCategoriesComponent}));
    ng1Gla.directive('glaFinanceCategoryModal', downgradeComponent({component: FinanceCategoryModalComponent}));
    ng1Gla.directive('glaTemplateBlockLearningGrant', downgradeComponent({component: TemplateBlockLearningGrantComponent}));
    ng1Gla.directive('glaBlockUsage', downgradeComponent({component: BlockUsageComponent}));
    ng1Gla.directive('glaEmailReports', downgradeComponent({component: EmailReportsComponent}));
    ng1Gla.directive('glaEmailReports', downgradeComponent({component: ClaimModalComponent}));
    ng1Gla.directive('glaProjectsPage', downgradeComponent({component: ProjectsPageComponent}));
    ng1Gla.directive('glaProgrammeAllocationsPage', downgradeComponent({component: ProgrammeAllocationsPageComponent}));
    ng1Gla.directive('glaEmailReports', downgradeComponent({component: ActualsMetadataModalComponent}));
    ng1Gla.directive('glaProjectOverviewPage', downgradeComponent({component: ProjectOverviewPageComponent}));
    ng1Gla.directive('glaHeaderStatus', downgradeComponent({component: HeaderStatusComponent}));
    ng1Gla.directive('glaOrganisationRequestAccessModalComponent', downgradeComponent({component: OrganisationRequestAccessModalComponent}));
    ng1Gla.directive('glaParentQuestionModalComponent', downgradeComponent({component: ParentQuestionModalComponent}));
    ng1Gla.directive('glaContractDetails', downgradeComponent({component: ContractDetailsComponent}));
    ng1Gla.directive('glaContractVariation', downgradeComponent({component: ContractVariationComponent}));
    ng1Gla.directive('glaFileUpload', downgradeComponent({component: FileUploadComponent}));
    ng1Gla.directive('glaFileUploadButton', downgradeComponent({component: FileUploadButtonComponent}))
    ng1Gla.directive('glaDeleteFileModal', downgradeComponent({component: DeleteFileModalComponent}))
    ng1Gla.directive('glaErrorModal', downgradeComponent({component: ErrorModalComponent}))
    ng1Gla.directive('glaActionDropdown', downgradeComponent({component: ActionDropdownComponent}))
    ng1Gla.directive('glaDateInput', downgradeComponent({component: DateInputComponent}))
    ng1Gla.directive('glaQuestionsPage', downgradeComponent({component: QuestionsPageComponent}))
    ng1Gla.directive('glaQuestions2', downgradeComponent({component: QuestionsComponent})) //TODO named '2' to prevent conflict in internal Qs page, remove '2' once that is migrated
    ng1Gla.directive('glaTeamMembers', downgradeComponent({component: TeamMembersComponent}))
    ng1Gla.directive('glaCancelPaymentActivityModal', downgradeComponent({component: FundingActivitiesCancelModalComponent}))
    ng1Gla.directive('glaContractTypes', downgradeComponent({component: ContractTypesComponent}))
    ng1Gla.directive('glaCancelPaymentQuarterModal', downgradeComponent({component: FundingQuarterCancelModalComponent}))
    ng1Gla.directive('glaTemplateBlockQuestions', downgradeComponent({component: TemplateBlockQuestionsComponent}));
    ng1Gla.directive('glaTemplateBlockUserDefinedOutputs', downgradeComponent({component: TemplateBlockUserDefinedOutputsComponent}));
    ng1Gla.directive('glaTemplateBlockProjectElements', downgradeComponent({component: TemplateBlockProjectElementsComponent}));
    ng1Gla.directive('glaTemplateBlockProjectObjectives', downgradeComponent({component: TemplateBlockProjectObjectivesComponent}));
    ng1Gla.directive('glaTemplateBlockOutputs', downgradeComponent({component: TemplateBlockOutputsComponent}));
    ng1Gla.directive('glaTemplateBlockDetails', downgradeComponent({component: TemplateBlockDetailsComponent}));

    //Services
    ng1Gla.factory('AuthService', downgradeInjectable(AuthService));
    ng1Gla.factory('FeatureToggleService', downgradeInjectable(FeatureToggleService));
    ng1Gla.factory('ReferenceDataService', downgradeInjectable(ReferenceDataService));
    ng1Gla.factory('TemplateService', downgradeInjectable(TemplateService));
    ng1Gla.factory('ConfirmationDialog', downgradeInjectable(ConfirmationDialogService));
    ng1Gla.factory('NavigationService', downgradeInjectable(NavigationService));
    ng1Gla.factory('OrganisationSapIdModalService', downgradeInjectable(OrganisationSapIdModalService));
    ng1Gla.factory('GlaUserService', downgradeInjectable(UserService));
    ng1Gla.factory('GlaSessionService', downgradeInjectable(SessionService));
    ng1Gla.factory('ToastrUtil', downgradeInjectable(ToastrUtilService));
    ng1Gla.factory('ErrorService', downgradeInjectable(ErrorService));
    ng1Gla.factory('GlaProjectBlockService', downgradeInjectable(ProjectBlockService));
    ng1Gla.factory('LoadingMaskService', downgradeInjectable(LoadingMaskService));
    ng1Gla.factory('ConfigurationService', downgradeInjectable(ConfigurationService));
    ng1Gla.factory('MetadataService', downgradeInjectable(MetadataService));
    ng1Gla.factory('OutputCategoryService', downgradeInjectable(OutputCategoryService));
    ng1Gla.factory('GlaProjectFundingService', downgradeInjectable(ProjectFundingService));
    ng1Gla.factory('GlaReportService', downgradeInjectable(ReportService));
    ng1Gla.factory('BroadcastService', downgradeInjectable(BroadcastService));
    ng1Gla.factory('GlaProjectService', downgradeInjectable(ProjectService));
    ng1Gla.factory('FinanceService', downgradeInjectable(FinanceService));
    ng1Gla.factory('TransitionService', downgradeInjectable(TransitionService));
    ng1Gla.factory('UserPasswordReset', downgradeInjectable(UserPasswordResetModalService));

    //External dependencies
    ng1Gla.factory('NgbModal', downgradeInjectable(NgbModal));
    ng1Gla.factory('TitleService', downgradeInjectable(Title));
    ng1Gla.factory('NgxPermissionsService', downgradeInjectable(NgxPermissionsService));
  }
}
