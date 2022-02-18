import {CUSTOM_ELEMENTS_SCHEMA, NgModule} from '@angular/core';
import {CommonModule, CurrencyPipe, DatePipe, DecimalPipe} from '@angular/common';
import {DeleteButtonComponent} from './delete-button/delete-button.component';
import {ToggleIconComponent} from './toggle-icon/toggle-icon.component';
import {ConfirmationDialogComponent} from './confirmation-dialog/confirmation-dialog.component'
import {ConfirmationDialogService} from './confirmation-dialog/confirmation-dialog.service';
import {FormsModule} from "@angular/forms";
import {IConfig, NgxMaskModule} from 'ngx-mask';
import {NumberTypeDirective} from './number-type/number-type.directive';
import {YesNoInputComponent} from './yes-no-input/yes-no-input.component';
import {ShowUpDownArrowButtonsComponent} from './show-up-down-arrow-buttons/show-up-down-arrow-buttons.component';
import {InfoTooltipComponent} from './info-tooltip/info-tooltip.component'
import {NgbDropdownModule, NgbPaginationModule, NgbTooltipModule} from "@ng-bootstrap/ng-bootstrap";
import {MarkdownComponent} from './markdown/markdown.component';
import {PageHeaderComponent} from './page-header/page-header.component';
import {NgxWebstorageModule} from "ngx-webstorage";
import {ProjectHeaderComponent} from './project-header/project-header.component';
import {SpinnerComponent} from './spinner/spinner.component';
import {IconNewComponent} from './icon-new/icon-new.component';
import {RemainingCharactersComponent} from './remaining-characters/remaining-characters.component';
import {MultiSelectComponent} from './multi-select/multi-select.component';
import {WellComponent} from './well/well.component';
import {MultiPanelComponent} from './multi-panel/multi-panel.component';
import {PaginationComponent} from './pagination/pagination.component';
import {TileComponent} from './tile/tile.component';
import {NgxPermissionsModule} from "ngx-permissions";
import {ConfirmationComponent} from './confirmation/confirmation.component';
import {ToastrModule} from "ngx-toastr";
import {ToastComponent} from './toastr/toast/toast.component';
import {LoadingMaskComponent} from './loading-mask/loading-mask.component';
import {LoadingMaskService} from "./loading-mask/loading-mask.service";
import {BoolPipe} from "../bool/bool.pipe";
import {FYearPipe} from "../fYear/f-year.pipe";
import {SectionHeaderComponent} from './section-header/section-header.component';
import {CheckboxFilterComponent} from './checkbox-filter/checkbox-filter.component';
import {SearchFieldComponent} from './search-field/search-field.component';
import {MobileDeviceWarningComponent} from './mobile-device-warning/mobile-device-warning.component';
import {OrgLookupDirective} from "../org-lookup/org-lookup.directive";
import { HeaderStatusComponent } from './header-status/header-status.component';
import { OverviewBlockComponent } from './overview-block/overview-block.component';
import { ErrorModalComponent } from './error/error-modal/error-modal.component';
import { ActionDropdownComponent } from './action-dropdown/action-dropdown.component';
import { DateInputComponent } from './date-input/date-input.component';

export const options: Partial<IConfig> | (() => Partial<IConfig>) = null;


@NgModule({
  declarations: [
    DeleteButtonComponent,
    ToggleIconComponent,
    ConfirmationDialogComponent,
    NumberTypeDirective,
    OrgLookupDirective,
    YesNoInputComponent,
    ShowUpDownArrowButtonsComponent,
    InfoTooltipComponent,
    MarkdownComponent,
    PageHeaderComponent,
    ProjectHeaderComponent,
    SpinnerComponent,
    IconNewComponent,
    RemainingCharactersComponent,
    MultiSelectComponent,
    WellComponent,
    MultiPanelComponent,
    PaginationComponent,
    TileComponent,
    ConfirmationComponent,
    ToastComponent,
    LoadingMaskComponent,
    BoolPipe,
    FYearPipe,
    SectionHeaderComponent,
    CheckboxFilterComponent,
    SearchFieldComponent,
    MobileDeviceWarningComponent,
    HeaderStatusComponent,
    OverviewBlockComponent,
    ErrorModalComponent,
    ActionDropdownComponent,
    DateInputComponent
  ],
  exports: [
    DeleteButtonComponent,
    ToggleIconComponent,
    YesNoInputComponent,
    ShowUpDownArrowButtonsComponent,
    IconNewComponent,
    RemainingCharactersComponent,
    MultiSelectComponent,
    WellComponent,
    PageHeaderComponent,
    ProjectHeaderComponent,

    NumberTypeDirective,
    OrgLookupDirective,

    NgxMaskModule,
    NgxWebstorageModule,
    NgbTooltipModule,
    NgbDropdownModule,
    NgxPermissionsModule,
    FormsModule,
    SpinnerComponent,
    BoolPipe,
    FYearPipe,
    SectionHeaderComponent,
    MarkdownComponent,
    CheckboxFilterComponent,
    InfoTooltipComponent,
    PaginationComponent,
    SearchFieldComponent,
    HeaderStatusComponent,
    OverviewBlockComponent,
    ActionDropdownComponent,
    DateInputComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    NgxMaskModule,
    NgxWebstorageModule,
    NgbTooltipModule,
    NgbDropdownModule,
    NgbPaginationModule,
    NgxPermissionsModule,
    ToastrModule
  ],
  providers: [
    ConfirmationDialogService,
    LoadingMaskService,
    DecimalPipe,
    CurrencyPipe,
    DatePipe
  ],
  //TODO: this one is for <ph-right> in page header but worth investigating/refactor with different selector like <ng-content selector=".pg-right"
  schemas: [ CUSTOM_ELEMENTS_SCHEMA ]
})
export class SharedModule {
}
