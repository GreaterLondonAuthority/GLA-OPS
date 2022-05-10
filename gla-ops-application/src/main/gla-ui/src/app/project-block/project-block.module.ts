import {NgModule} from '@angular/core';
import {CommonModule, DatePipe} from '@angular/common';
import {VersionHistoryModalComponent} from './version-history-modal/version-history-modal.component';
import {InternalProjectAdminBlockComponent} from './internal-project-admin-block/internal-project-admin-block.component';
import {FormsModule} from "@angular/forms";
import {SharedModule} from "../shared/shared.module";
import {DesignStandardsBlockComponent} from './design-standards-block/design-standards-block.component';
import {AffordableHomesBlockComponent} from './affordable-homes-block/affordable-homes-block.component';
import {AffordableHomesTableComponent} from './affordable-homes-block/affordable-homes-table/affordable-homes-table.component';
import {IndicativeGrantRequestedTableComponent} from './affordable-homes-block/indicative-grant-requested-table/indicative-grant-requested-table.component';
import { IndicativeCostsAndContributionsTableComponent } from './affordable-homes-block/indicative-costs-and-contributions-table/indicative-costs-and-contributions-table.component';
import { IndicativeCostsAndContributionsSummaryComponent } from './affordable-homes-block/indicative-costs-and-contributions-summary/indicative-costs-and-contributions-summary.component';
import { UnitDetailsBlockComponent } from './unit-details-block/unit-details-block.component';
import { ProfiledUnitsModalComponent } from './unit-details-block/profiled-units-modal/profiled-units-modal.component';
import {FileUploadComponent} from '../file-upload/file-upload.component';


@NgModule({
    declarations: [
        VersionHistoryModalComponent,
        InternalProjectAdminBlockComponent,
        DesignStandardsBlockComponent,
        AffordableHomesBlockComponent,
        AffordableHomesTableComponent,
        IndicativeGrantRequestedTableComponent,
        IndicativeCostsAndContributionsTableComponent,
        IndicativeCostsAndContributionsSummaryComponent,
        UnitDetailsBlockComponent,
        ProfiledUnitsModalComponent
    ],
    imports: [
        CommonModule,
        FormsModule,
        SharedModule
    ],
    exports: [
      IndicativeGrantRequestedTableComponent,
      ProfiledUnitsModalComponent
    ],
    providers: [DatePipe]
})
export class ProjectBlockModule {
}
