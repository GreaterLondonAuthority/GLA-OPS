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

export class Downgrades {
  downgrade() {
    const ng1Gla = getAngularJSGlobal().module('GLA');

    //Components
    ng1Gla.directive('deleteButton', downgradeComponent({component: DeleteButtonComponent}));
    ng1Gla.directive('templateBlockMilestones', downgradeComponent({component: TemplateBlockMilestonesComponent}));
    ng1Gla.directive('toggleIcon', downgradeComponent({component: ToggleIconComponent}));
    ng1Gla.directive('glaYesNoInput', downgradeComponent({component: YesNoInputComponent}));
    ng1Gla.directive('glaTeamsDefaultAccessList', downgradeComponent({component: TeamsDefaultAccessListComponent}));
    ng1Gla.directive('showUpDownArrowButtons', downgradeComponent({component: ShowUpDownArrowButtonsComponent}));
    ng1Gla.directive('glaInfoTooltip', downgradeComponent({component: InfoTooltipComponent}));
    ng1Gla.directive('glaMarkdown', downgradeComponent({component: MarkdownComponent}));
    ng1Gla.directive('glaPageHeader', downgradeComponent({component: PageHeaderComponent}));
    ng1Gla.directive('glaProjectHeader', downgradeComponent({component: ProjectHeaderComponent}));
    ng1Gla.directive('glaJsonViewer', downgradeComponent({component: JsonViewerComponent}));
    ng1Gla.directive('glaSpinner', downgradeComponent({component: SpinnerComponent}));
    ng1Gla.directive('glaIconNew', downgradeComponent({component: IconNewComponent}));
    ng1Gla.directive('glaRemainingCharacters', downgradeComponent({component: RemainingCharactersComponent}));
    ng1Gla.directive('glaMultiSelect', downgradeComponent({component: MultiSelectComponent}));

    //Services
    ng1Gla.factory('AuthService', downgradeInjectable(AuthService));
    ng1Gla.factory('FeatureToggleService', downgradeInjectable(FeatureToggleService));
    ng1Gla.factory('ReferenceDataService', downgradeInjectable(ReferenceDataService));
    ng1Gla.factory('ConfirmationDialog', downgradeInjectable(ConfirmationDialogService));
    ng1Gla.factory('NavigationService', downgradeInjectable(NavigationService));

    //External dependencies
    ng1Gla.factory('NgbModal', downgradeInjectable(NgbModal));
    ng1Gla.factory('TitleService', downgradeInjectable(Title));
  }
}
