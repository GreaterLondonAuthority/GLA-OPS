import {CUSTOM_ELEMENTS_SCHEMA, NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {TemplateBlockMilestonesComponent} from './template-block-milestones/template-block-milestones.component';
import {TemplateBlockMilestonesService} from "./template-block-milestones/template-block-milestones.service";
import {SharedModule} from "../shared/shared.module";
import {FormsModule} from "@angular/forms";
import {NgbModule} from '@ng-bootstrap/ng-bootstrap';
import {ProcessingRouteModalComponent} from './processing-route-modal/processing-route-modal.component';
import {ProcessingRouteModalService} from './processing-route-modal/processing-route-modal.service';
import {TemplateMilestoneModalComponent} from './template-milestone-modal/template-milestone-modal.component';
import {JsonViewerComponent} from './json-viewer/json-viewer.component';
import {NgJsonEditorModule} from "ang-jsoneditor";
import {TemplateInternalBlockComponent} from './template-internal-block/template-internal-block.component';
import {TemplateInternalBlocksComponent} from './template-internal-blocks/template-internal-blocks.component';
import {FundingSourceTableComponent} from './funding-source-table/funding-source-table.component';
import {FundingSourceModalComponent} from './funding-source-modal/funding-source-modal.component';
import {TemplateBlockOtherFundingComponent} from './template-block-other-funding/template-block-other-funding.component';
import {TemplateBlockGrantSourceComponent} from './template-block-grant-source/template-block-grant-source.component';
import {TemplateBlockDeliveryPartnersComponent} from './template-block-delivery-partners/template-block-delivery-partners.component';
import {TemplateBlockFundingComponent} from './template-block-funding/template-block-funding.component';
import {TemplateBlockLearningGrantComponent} from './template-block-learning-grant/template-block-learning-grant.component';
import {TemplateBlockStartsAndCompletionsComponent} from './template-block-starts-and-completions/template-block-starts-and-completions.component';
import {ParentQuestionModalComponent} from './template-block-questions/parent-question-modal/parent-question-modal.component';
import {TemplateInternalBlockQuestionsCommentsComponent} from './template-internal-block-questions-comments/template-internal-block-questions-comments.component';
import {SectionModalComponent} from './section-modal/section-modal.component';
import {QuestionModalComponent} from './question-modal/question-modal.component';
import { TemplateExternalBlocksComponent } from './template-external-blocks/template-external-blocks.component';
import { TemplateExternalBlockComponent } from './template-external-block/template-external-block.component';
import { TemplateBlockQuestionsComponent } from './template-block-questions/template-block-questions.component';
import { TemplateBlockUserDefinedOutputsComponent } from './template-block-user-defined-outputs/template-block-user-defined-outputs.component';
import { TemplateBlockProjectElementsComponent } from './template-block-project-elements/template-block-project-elements.component';
import { TemplateBlockProjectObjectivesComponent } from './template-block-project-objectives/template-block-project-objectives.component';
import { LearningGrantLabelsModalComponent } from './template-block-learning-grant/learning-grant-labels-modal/learning-grant-labels-modal.component';
import { TemplateBlockRepeatingEntityComponent } from './template-block-repeating-entity/template-block-repeating-entity.component';
import { TemplateBlockOutputsComponent } from './template-block-outputs/template-block-outputs.component';
import { TemplateBlockDetailsComponent } from './template-block-details/template-block-details.component';


@NgModule({
  declarations: [
    TemplateBlockMilestonesComponent,
    ProcessingRouteModalComponent,
    TemplateMilestoneModalComponent,
    JsonViewerComponent,
    TemplateInternalBlockComponent,
    TemplateInternalBlocksComponent,
    FundingSourceTableComponent,
    FundingSourceModalComponent,
    TemplateBlockOtherFundingComponent,
    TemplateBlockGrantSourceComponent,
    TemplateBlockDeliveryPartnersComponent,
    TemplateBlockFundingComponent,
    TemplateBlockLearningGrantComponent,
    TemplateBlockStartsAndCompletionsComponent,
    ParentQuestionModalComponent,
    TemplateExternalBlocksComponent,
    ParentQuestionModalComponent,
    TemplateInternalBlockQuestionsCommentsComponent,
    SectionModalComponent,
    QuestionModalComponent,
    TemplateExternalBlockComponent,
    TemplateBlockQuestionsComponent,
    TemplateBlockUserDefinedOutputsComponent,
    TemplateBlockProjectElementsComponent,
    TemplateBlockProjectObjectivesComponent,
    LearningGrantLabelsModalComponent,
    TemplateBlockRepeatingEntityComponent,
    TemplateBlockOutputsComponent,
    TemplateBlockDetailsComponent
  ],
  exports: [
    TemplateBlockMilestonesComponent,
    JsonViewerComponent
  ],
  imports: [
    CommonModule,
    SharedModule,
    FormsModule,
    NgbModule,
    NgJsonEditorModule
  ],
  providers: [
    TemplateBlockMilestonesService,
    ProcessingRouteModalService
  ],
  schemas: [ CUSTOM_ELEMENTS_SCHEMA ]
})
export class TemplateConfigurationModule {
}
