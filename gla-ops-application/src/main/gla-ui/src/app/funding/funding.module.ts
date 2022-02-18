import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FundingActivitiesClaimModalComponent} from './funding-activities-claim-modal/funding-activities-claim-modal.component';
import {FundingClaimModalComponent} from './funding-claim-modal/funding-claim-modal.component';
import {FundingAllActivitiesClaimModalComponent} from './funding-all-activities-claim-modal/funding-all-activities-claim-modal.component';
import {SharedModule} from "../shared/shared.module";
import { FundingActivitiesCancelModalComponent } from './funding-activities-cancel-modal/funding-activities-cancel-modal.component';
import { FundingQuarterCancelModalComponent } from './funding-quarter-cancel-modal/funding-quarter-cancel-modal.component';


@NgModule({
  declarations: [
    FundingActivitiesClaimModalComponent,
    FundingClaimModalComponent,
    FundingAllActivitiesClaimModalComponent,
    FundingActivitiesCancelModalComponent,
    FundingQuarterCancelModalComponent
  ],
  imports: [
    CommonModule,
    SharedModule
  ]
})
export class FundingModule {
}
