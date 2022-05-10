import {Component, Input, OnInit} from '@angular/core';
import {filter} from "lodash-es";
import {UserService} from "../../user/user.service";

@Component({
  selector: 'gla-team-members',
  templateUrl: './team-members.component.html',
  styleUrls: ['./team-members.component.scss']
})
export class TeamMembersComponent implements OnInit {

  @Input() org: any;

  usersSpendThreshold: any;
  tooltipText:string = "To add or remove Team members, visit the All Users page."

  constructor(private userService: UserService) { }

  ngOnInit(): void {
      this.userService.getUserThresholdsByOrgId(this.org.id).subscribe(resp => {
        this.usersSpendThreshold = resp as any[];
      }
    );
  }

  findUserRoles(username){
    let users = filter(this.org.users, {username: username})
    return filter(users[0].roles, {organisationId:this.org.id})
  }

  findUserSpendThreshold(username) {
    if (this.usersSpendThreshold && this.usersSpendThreshold.length >0) {
      let currentUserSpendThreshold = filter(this.usersSpendThreshold, {id: {username: username, organisationId: this.org.id}});
      return currentUserSpendThreshold.length > 0 ? currentUserSpendThreshold[0].approvedThreshold : null
    }
  }

}
