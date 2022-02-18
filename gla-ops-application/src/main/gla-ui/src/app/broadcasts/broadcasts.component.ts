import {Component, Input, OnInit} from '@angular/core';
import {NavigationService} from "../navigation/navigation.service";
import {BroadcastService} from './broadcast.service';
import {sortBy} from "lodash-es";
import {UserService} from "../user/user.service";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {DeleteModalComponent} from "./delete-modal/delete-modal.component";

@Component({
  selector: 'gla-broadcasts',
  templateUrl: './broadcasts.component.html',
  styleUrls: ['./broadcasts.component.scss']
})
export class BroadcastsComponent implements OnInit {

  @Input() broadcasts: any[];
  canAddBroadcast: any

  constructor(private navigationService: NavigationService,
              private broadcastService: BroadcastService,
              private ngbModal: NgbModal,
              private userService: UserService) { }

  ngOnInit(): void {
    this.canAddBroadcast = this.userService.hasPermission('broadcast.create');
    this.broadcasts = sortBy(this.broadcasts, ['modifiedOn']).reverse();
  }

  addBroadcast() {
    this.navigationService.goToUiRouterState('broadcast-create');
  }

  viewBroadcast(broadcastId){
    this.navigationService.goToUiRouterState('broadcast', {broadcastId: broadcastId});
  }

  deleteBroadcast(broadcast) {
    const modal = this.ngbModal.open(DeleteModalComponent);

    modal.componentInstance.broadcast = broadcast;

    modal.result.then(() => {
      this.broadcastService.deleteBroadcast(broadcast.id).subscribe(() => {
        this.refresh();
      }, (error) => {
      });
    });
  }

  refresh() {
    this.broadcastService.getBroadcasts().subscribe( resp => {
      this.broadcasts = resp as any[];
      }
    )
  }

}
