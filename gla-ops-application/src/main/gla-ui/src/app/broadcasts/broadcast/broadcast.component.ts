import {Component, Input, OnInit} from '@angular/core';
import {NavigationService} from "../../navigation/navigation.service";
import {BroadcastService} from "../broadcast.service";
import {filter, find, sortBy} from "lodash-es";
import {ProjectService} from "../../project/project.service";
import {UserService} from "../../user/user.service";

@Component({
  selector: 'gla-broadcast',
  templateUrl: './broadcast.component.html',
  styleUrls: ['./broadcast.component.scss']
})
export class BroadcastComponent implements OnInit {

  @Input() broadcast: any
  @Input() programmes: any[]
  @Input() projectStates: { status: string }[]
  managingOrganisations: any[]
  templates: any[]
  projectStatuses: { name: string; label: string }[] = []

  constructor(private broadcastService: BroadcastService,
              private navigationService: NavigationService,
              private projectService: ProjectService,
              private userService: UserService) { }

  ngOnInit(): void {
    this.managingOrganisations = this.userService.currentUser().organisations
                                 .filter(org => org.isManagingOrganisation == true)
    if (this.broadcast == null) {
      this.broadcast = {
        mainProjectContacts: true
      };
    }

    this.initProgrammes();
    this.initProjectStatuses();
  }

  initProgrammes() {
    this.programmes = sortBy(this.programmes, 'name');
  }

  private initProjectStatuses() {
    this.projectStatuses = this.projectService.toProjectStatuses(this.projectStates);
  }

  isFormValid() {
    return (this.managingOrganisations.length <= 1 || this.broadcast.managingOrganisationId != null)
      && this.broadcast.programmeId != null
      && this.broadcast.templateIds != null
      && this.broadcast.projectStatus != null
      && this.broadcast.subject != null && this.broadcast.subject !== ''
      && this.broadcast.body != null && this.broadcast.body !== ''
      && this.broadcast.signOff != null && this.broadcast.signOff !== '';
  }

  requestApproval() {
    if (this.managingOrganisations.length == 1) {
      this.broadcast.managingOrganisationId = this.managingOrganisations[0].id;
    }
    this.broadcastService.createBroadcast(this.broadcast).subscribe( () => {
      this.goToBroadcasts();
    },(error) => {
    });
  }

  approve() {
    this.broadcastService.approveBroadcast(this.broadcast.id).subscribe( () => {
      this.goToBroadcasts();
    },(error) => {
    });
  }

  isPendingApproval() {
    return this.broadcast.status === 'Pending Approval';
  }

  isApproved() {
    return this.broadcast.status === 'Approved';
  }

  goToBroadcasts() {
    this.navigationService.goToUiRouterState('broadcasts');
  }

  setTemplates() {
    this.broadcast.templateIds = filter(this.templates, {model: true}).map(t => t.id);
  }

  getBroadcastContentTooltipText() {
    return 'All Broadcasts are prefixed as follows:\n\n' +
      'Dear [name]\n\n' +
      'Re: [Project ID], [Project Title], [Organisation ID], [Organisation name]';
  }

  isEditable() {
    return !this.broadcast.id
  }

  getTemplateName(templateId: any) {
    let templateName = '';
    this.programmes.forEach(programme => {
      programme.templates.forEach(template => {
        if (template.id == templateId) {
          templateName = template.name;
        }
      });
    });
    return templateName;
  }

  updateTemplatesDropdown() {
    this.templates = [];
    if (this.broadcast.programmeId) {
      let programme = find(this.programmes, {id: this.broadcast.programmeId});
      programme.templates.forEach(template => {
        this.templates.push({
          id: template.id,
          label: template.name
        });
      });
    }
  }

}
