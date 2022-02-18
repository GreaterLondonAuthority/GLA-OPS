import {CUSTOM_ELEMENTS_SCHEMA, NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ProjectAssignModalComponent} from './project-assign-modal/project-assign-modal.component';
import {SharedModule} from "../shared/shared.module";
import { ProjectTransferModalComponent } from './project-transfer-modal/project-transfer-modal.component';
import { NewProjectPageComponent } from './new-project-page/new-project-page.component';
import { ProjectAbandonModalComponent } from './project-abandon-modal/project-abandon-modal.component';
import { ProjectShareModalComponent } from './project-share-modal/project-share-modal.component';
import { ProjectLabelModalComponent } from './project-label-modal/project-label-modal.component';
import { ProjectsPageComponent } from './projects-page/projects-page.component';
import { ProjectOverviewPageComponent } from './project-overview-page/project-overview-page.component';
import { ProjectOverviewBlockComponent } from './project-overview-block/project-overview-block.component';
import { ProjectHistoryComponent } from './project-history/project-history.component';
import {NgbAccordionModule} from "@ng-bootstrap/ng-bootstrap";
import { ProjectPaymentConfirmModalComponent } from './project-payment-confirm-modal/project-payment-confirm-modal.component';
import { ProgrammeAllocationsPageComponent } from './programme-allocations-page/programme-allocations-page.component';


@NgModule({
  declarations: [
    ProjectAssignModalComponent,
    ProjectTransferModalComponent,
    NewProjectPageComponent,
    ProjectAbandonModalComponent,
    ProjectShareModalComponent,
    ProjectLabelModalComponent,
    ProjectsPageComponent,
    ProjectOverviewPageComponent,
    ProjectOverviewBlockComponent,
    ProjectHistoryComponent,
    ProjectPaymentConfirmModalComponent,
    ProgrammeAllocationsPageComponent
  ],
  imports: [
    CommonModule,
    SharedModule,
    NgbAccordionModule
  ],
  schemas: [ CUSTOM_ELEMENTS_SCHEMA ]
})
export class ProjectModule {
}
