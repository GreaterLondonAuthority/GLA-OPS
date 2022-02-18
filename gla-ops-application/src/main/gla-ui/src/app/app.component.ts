import {Component, OnInit} from '@angular/core';
import {AutoResume, DEFAULT_INTERRUPTSOURCES, Idle} from "@ng-idle/core";
import {Keepalive} from "@ng-idle/keepalive";
import {NgbModal, NgbModalRef} from "@ng-bootstrap/ng-bootstrap";
import {SessionTimeoutModalComponent} from "./session-timeout-modal/session-timeout-modal.component";
import {UserService} from "./user/user.service";
import {MetadataService} from "./metadata/metadata.service";

@Component({
  selector: 'gla-app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit{
  private sessionTimeoutModal: NgbModalRef;

  constructor(private idle: Idle,
              private keepalive: Keepalive,
              private ngbModal: NgbModal,
              private userService: UserService,
              private metadataService: MetadataService) {
  }

  ngOnInit(): void {
    this.configureSessionTimeouts();
  }

  configureSessionTimeouts(){
    this.userService.setupUserSession();
    this.idle.onIdleEnd.subscribe(() => {
      if(this.sessionTimeoutModal){
        this.sessionTimeoutModal.close();
      }
    });

    this.idle.onTimeout.subscribe(() => {
      if(this.sessionTimeoutModal){
        this.sessionTimeoutModal.close();
      }
      this.userService.logout('Sorry, your session has timed out').subscribe(()=>{});
    });

    this.idle.onIdleStart.subscribe(() => {
      if(this.sessionTimeoutModal){
        this.sessionTimeoutModal.close();
      }
      this.sessionTimeoutModal = this.ngbModal.open(SessionTimeoutModalComponent)
    });

    this.keepalive.onPing.subscribe(() => {
      this.metadataService.fireMetadataUpdate();
    });
  }
}
