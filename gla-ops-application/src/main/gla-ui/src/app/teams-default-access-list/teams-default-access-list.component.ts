import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';

@Component({
  selector: 'gla-teams-default-access-list',
  templateUrl: './teams-default-access-list.component.html',
  styleUrls: ['./teams-default-access-list.component.scss']
})
export class TeamsDefaultAccessListComponent implements OnInit {

  @Input() programmeManagingOrganisationName
  @Input() templateName
  @Input() teams
  @Input() moAccess = false;
  @Input() readOnly: boolean
  @Output() onTeamDefaultAccessChange = new EventEmitter<any>();
  @Output() onManagingOrgAccessChange = new EventEmitter<any>();

  constructor() {

  }

  ngOnInit(): void {
  }

  numberOfTeamsWithAccess() {
    return this.teams.filter( t => t.hasDefaultAccess).length
  }

  getId(name) {
    return name.toLowerCase().replace(/ /g, '-');
  }

  canEditTeam(team) {
   return !(!this.moAccess && this.numberOfTeamsWithAccess() == 1 && team.hasDefaultAccess)
  }

  onDefaultAccessChange(team) {
    this.onTeamDefaultAccessChange.emit(team)
  }

  onManagingOrganisationAccessChange() {
    this.onManagingOrgAccessChange.emit(this.moAccess);
  }

}
