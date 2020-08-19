import {CUSTOM_ELEMENTS_SCHEMA, NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
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
import {NgbTooltipModule} from "@ng-bootstrap/ng-bootstrap";
import {NgbDropdownModule} from "@ng-bootstrap/ng-bootstrap";
import {MarkdownComponent} from './markdown/markdown.component';
import { PageHeaderComponent } from './page-header/page-header.component';
import {NgxWebstorageModule} from "ngx-webstorage";
import { ProjectHeaderComponent } from './project-header/project-header.component';
import { SpinnerComponent } from './spinner/spinner.component';
import { IconNewComponent } from './icon-new/icon-new.component';
import { RemainingCharactersComponent } from './remaining-characters/remaining-characters.component';
import { MultiSelectComponent } from './multi-select/multi-select.component';

export const options: Partial<IConfig> | (() => Partial<IConfig>) = null;


@NgModule({
  declarations: [
    DeleteButtonComponent,
    ToggleIconComponent,
    ConfirmationDialogComponent,
    NumberTypeDirective,
    YesNoInputComponent,
    ShowUpDownArrowButtonsComponent,
    InfoTooltipComponent,
    MarkdownComponent,
    PageHeaderComponent,
    ProjectHeaderComponent,
    SpinnerComponent,
    IconNewComponent,
    RemainingCharactersComponent,
    MultiSelectComponent
  ],
  exports: [
    DeleteButtonComponent,
    ToggleIconComponent,
    YesNoInputComponent,
    ShowUpDownArrowButtonsComponent,
    IconNewComponent,
    RemainingCharactersComponent,
    MultiSelectComponent,

    NumberTypeDirective,

    NgxMaskModule,
    NgxWebstorageModule,
    NgbTooltipModule,
    NgbDropdownModule
  ],
  imports: [
    CommonModule,
    FormsModule,
    NgxMaskModule.forRoot(),
    NgxWebstorageModule.forRoot(),
    NgbTooltipModule,
    NgbDropdownModule
  ],
  providers: [
    ConfirmationDialogService
  ],
  //TODO: this one is for <ph-right> in page header but worth investigating/refactor with different selector like <ng-content selector=".pg-right"
  schemas: [ CUSTOM_ELEMENTS_SCHEMA ]
})
export class SharedModule {
}
