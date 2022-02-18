import {Component, Input, OnInit} from '@angular/core';
import {UserService} from "../user/user.service";
import {LoadingMaskService} from "../shared/loading-mask/loading-mask.service";
import {Location, HashLocationStrategy, LocationStrategy} from "@angular/common";
import {NavigationService} from "../navigation/navigation.service";
import {PostLoginService} from "../post-login/post-login.service";
import {ConfirmationDialogService} from "../shared/confirmation-dialog/confirmation-dialog.service";

@Component({
  selector: 'gla-login-page',
  templateUrl: './login-page.component.html',
  styleUrls: ['./login-page.component.scss'],
  providers: [Location, {provide: LocationStrategy, useClass: HashLocationStrategy}]
})
export class LoginPageComponent implements OnInit {
  @Input() message: string;
  @Input() reasonSuccess: string;
  @Input() reasonError: string;
  @Input() redirectUrl: string;

  error = false;
  errorMessage: any;
  uname: string;
  pass: string;
  unamePattern = /^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/;

  constructor(private userService: UserService,
              private loadingMaskService: LoadingMaskService,
              private location: Location,
              private navigationService: NavigationService,
              private postLoginService: PostLoginService,
              private confirmationDialog: ConfirmationDialogService) {
  }

  ngOnInit(): void {
  }

  submit() {
    this.loadingMaskService.showLoadingMask(true);
    this.userService.login(this.uname, this.pass).subscribe((user) => {
        this.error = false;
        if (this.redirectUrl) {
          this.location.go(this.redirectUrl)
        } else if (user.data.primaryRole === 'Admin') {
          this.navigationService.goToUiRouterState('projects');
        } else {
          this.navigationService.goToUiRouterState('user');
        }

        let currentUser = this.userService.currentUser();
        if (currentUser.approved) {
          // Any post login api calls, should be performed here
          this.postLoginService.checkForDeprecatedOrganisationsType().subscribe((resp) => {
            if (resp) {
              this.confirmationDialog.warn(resp);
            }
          }, (err) => {
            console.error(err)
          });
        }

      }, (rsp) => {
        if (rsp.error === 'PASSWORD_EXPIRED') {
          this.navigationService.goToUiRouterState('password-expired', {username: this.uname})
        } else {
          this.error = true;
          this.errorMessage = rsp.error || 'Sorry, your email and password combination is not recognised';
          this.loadingMaskService.showLoadingMask(false)
          console.error(rsp);
        }
      }
    );
  };

  clearErrors () {
    this.error = false;
  };

  goTo(uiRouterState: string) {
    this.navigationService.goToUiRouterState(uiRouterState);
  }
}
