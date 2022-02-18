import {Component, Input, OnInit} from '@angular/core';
import {ProjectService} from "../project.service";
import {UserService} from "../../user/user.service";
import {filter, find, forIn, orderBy} from "lodash-es";
import {NavigationService} from "../../navigation/navigation.service";

@Component({
  selector: 'gla-new-project-page',
  templateUrl: './new-project-page.component.html',
  styleUrls: ['./new-project-page.component.scss']
})
export class NewProjectPageComponent implements OnInit {

  @Input() programmes: any[];

  user: any;
  organisations: any[];
  templates: any[];
  title: string;
  selectedOrganisation: any;
  selectedProgramme: any;
  selectedTemplate: any;
  isSaving: boolean;
  showMissingProfileOrgAdmin: boolean;
  showMissingProfileProjectEditor: boolean;
  showPendingProfileProjectEditor: boolean;
  availableOrganisationsForProgramme: any[];
  availableOrganisationsForProgrammeAndUser: any[];
  pendingProfile: any;
  showMaxProjectsForTemplateError: boolean;

  constructor(private projectService: ProjectService,
              private userService: UserService,
              private navigationService: NavigationService) { }

  ngOnInit(): void {
    this.user = this.userService.currentUser();

    // this.$state = $state;
    this.organisations = this.user.organisations || [];
    this.templates = [];
    this.title = null;
    this.selectedOrganisation = null;
    this.selectedProgramme = null;
    this.selectedTemplate = null;
    this.isSaving = false;
    this.showMissingProfileOrgAdmin = false;
    this.showMissingProfileProjectEditor = false;
    this.showPendingProfileProjectEditor = false;
    this.availableOrganisationsForProgramme = [];
    this.availableOrganisationsForProgrammeAndUser = [];
  }

  selectProgramme(programme) {
    this.showMissingProfileOrgAdmin = false;
    this.showMissingProfileProjectEditor = false;
    this.showPendingProfileProjectEditor = false;

    this.availableOrganisationsForProgramme = filter(this.organisations, (org) => {
      return org.managingOrganisationId === programme.managingOrganisationId || org.id === programme.managingOrganisationId;
    });

    this.availableOrganisationsForProgrammeAndUser = [];
    forIn(this.availableOrganisationsForProgramme, (value, key) => {
      if(this.userService.hasPermission('proj.create', value.id)) {
        this.availableOrganisationsForProgrammeAndUser.push(value);
      }
    });

    this.selectedOrganisation = this.availableOrganisationsForProgrammeAndUser.length === 1 ? this.availableOrganisationsForProgrammeAndUser[0] : null;

    if(!this.availableOrganisationsForProgrammeAndUser.length){
      this.pendingProfile = find(this.user.roles, {managingOrganisationId: programme.managingOrganisationId, orgStatus: 'Pending'});

      if(this.pendingProfile) {
        this.showPendingProfileProjectEditor = true;
      }
    }

    this.templates = filter(orderBy(programme.templates, 'name'), {status: 'Active'});
  }

  canProjectBeAssignedToTemplate(){
    if(this.selectedTemplate && this.selectedOrganisation) {
      this.projectService.canProjectBeAssignedToTemplate(this.selectedTemplate.id, this.selectedOrganisation.id).subscribe(rsp => {
        this.showMaxProjectsForTemplateError = rsp === false;
      });
    }
  }

  submit(){
    if(this.isSaving){
      return;
    }
    this.isSaving = true;
    let data = {
      title: this.title,
      programme: {
        id: this.selectedProgramme.id
      },
      template: {
        id: this.selectedTemplate.id
      },

    } as any;
    if (this.selectedOrganisation) {
      data.organisation = {
        id: this.selectedOrganisation.id
      };
    }

    this.projectService.createProject(data).subscribe(resp => {
        this.isSaving = false;
        if (!resp) return;
        const createdProjectId = resp;
        this.navigationService.goToUiRouterState('project-overview', {'projectId': createdProjectId})
      }, (err) => {
      this.isSaving = false;
      console.error(err);
    });
  }

  goTo(stateName, params?) {
    this.navigationService.goToUiRouterState(stateName, params)
  }
}
