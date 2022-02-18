import {Component, Input, OnInit} from '@angular/core';
import {UserService} from "../user/user.service";
import {NavigationService} from "../navigation/navigation.service";

@Component({
  selector: 'gla-request-password-reset-page',
  templateUrl: './request-password-reset-page.component.html',
  styleUrls: ['./request-password-reset-page.component.scss']
})
export class RequestPasswordResetPageComponent implements OnInit {

  isRequestSuccess = false;
  userEmail: string = null;
  emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/;

  @Input() errorMessage: string;

  constructor(private userService: UserService,
              private navigationService: NavigationService) { }

  ngOnInit(): void {
  }

  onSubmit() {
    this.userService.requestPasswordReset(this.userEmail).subscribe();
    this.isRequestSuccess = true;
  }

  onCancel() {
    this.navigationService.goToUiRouterState('home')
  }
}
