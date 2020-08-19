import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {TemplateBlockMilestonesComponent} from './template-block-milestones/template-block-milestones.component';
import {TemplateBlockMilestonesService} from "./template-block-milestones/template-block-milestones.service";
import {SharedModule} from "../shared/shared.module";
import {FormsModule} from "@angular/forms";
import {NgbModule} from '@ng-bootstrap/ng-bootstrap';
import {ProcessingRouteModalComponent} from './processing-route-modal/processing-route-modal.component';
import {ProcessingRouteModalService} from './processing-route-modal/processing-route-modal.service';
import {TemplateMilestoneModalComponent} from './template-milestone-modal/template-milestone-modal.component';
import { JsonViewerComponent } from './json-viewer/json-viewer.component';
import {NgJsonEditorModule} from "ang-jsoneditor";


@NgModule({
  declarations: [
    TemplateBlockMilestonesComponent,
    ProcessingRouteModalComponent,
    TemplateMilestoneModalComponent,
    JsonViewerComponent
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
  ]
})
export class TemplateConfigurationModule {
}
