import {Component, OnDestroy, OnInit} from '@angular/core';
import {DEFAULT_INTERRUPTSOURCES, Idle} from "@ng-idle/core";
import {UserService} from "../user/user.service";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {Subscription} from "rxjs";
declare var moment: any;


@Component({
  selector: 'gla-session-timeout-modal',
  templateUrl: './session-timeout-modal.component.html',
  styleUrls: ['./session-timeout-modal.component.scss']
})
export class SessionTimeoutModalComponent implements OnInit, OnDestroy {
  private timeoutWarningSubscription: Subscription;
  timeLeft: string

  constructor(private idle: Idle,
              private userService: UserService,
              public activeModal: NgbActiveModal) {
    this.idle.setInterrupts([]);
    this.timeoutWarningSubscription = this.idle.onTimeoutWarning.subscribe((countdown) => {
      this.timeLeft = this.formatTime(countdown);
    });
  }

  ngOnInit(): void {
    // this.idle.setInterrupts([]);
    // this.timeoutWarningSubscription = this.idle.onTimeoutWarning.subscribe((countdown) => {
    //   this.timeLeft = this.formatTime(countdown);
    // });
  }

  ngOnDestroy(): void {
    this.timeoutWarningSubscription.unsubscribe();
  }

  resume() {
    this.idle.setInterrupts(DEFAULT_INTERRUPTSOURCES);
    this.idle.watch();
    this.activeModal.dismiss('cancel');
  };

  logout () {
    this.userService.logout().subscribe();
    this.activeModal.dismiss('cancel');
  };

  formatTime(secondsLeft) {
    let duration = moment.duration(secondsLeft, 's');
    let minutes = duration.minutes();
    let seconds = duration.seconds();
    let minutesText = minutes === 1? 'minute' : 'minutes';
    let secondsText = seconds === 1? 'second' : 'seconds';
    return `${minutes} ${minutesText} ${seconds} ${secondsText}`;
  }
}
