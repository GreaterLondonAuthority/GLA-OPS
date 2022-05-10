import {Component, Input, OnInit} from '@angular/core';
import {UserService} from "../../user/user.service";
import {startCase} from "lodash-es";
import {NavigationService} from "../../navigation/navigation.service";
import {ProjectBlockService} from "../project-block.service";

@Component({
  selector: 'gla-internal-project-admin-block',
  templateUrl: './internal-project-admin-block.component.html',
  styleUrls: ['./internal-project-admin-block.component.scss']
})
export class InternalProjectAdminBlockComponent implements OnInit {

  @Input() project
  @Input() block
  readOnly: boolean = true
  editable: boolean
  title: string
  $ctrl: any

  constructor(private projectBlockService: ProjectBlockService,
              private userService: UserService,
              private navigationService: NavigationService) {}

  ngOnInit(): void {
    this.editable = this.userService.hasPermission('proj.edit.internal.blocks');
    this.readOnly = true;
    this.title = startCase(this.block.blockDisplayName.toLowerCase());
    this.$ctrl = this;
  }

  back() {
    this.navigationService.goToUiRouterState('project-overview', {projectId: this.project.id}, {reload: true});
  }

  edit() {
    this.readOnly = false;
  }

  stopEditing() {
    this.projectBlockService.updateInternalBlock(this.project.id, this.block).subscribe(() => {
      this.readOnly = true;
    })
  }

  combinedLengthExceeded() {
    let sizeExceeded = (this.block.projectShortName?.length + this.block.organisationShortName?.length) >= 35;
    this.editable = !sizeExceeded;
    return sizeExceeded;
  }

}
