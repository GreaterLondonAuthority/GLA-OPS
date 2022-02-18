import { Component, OnInit, Input } from '@angular/core';
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import { UserService } from '../user/user.service';

@Component({
  selector: 'gla-user-password-reset-modal',
  templateUrl: './user-password-reset-modal.component.html',
  styleUrls: ['./user-password-reset-modal.component.scss']
})
export class UserPasswordResetModalComponent implements OnInit {

  @Input() config: any
  strength: number = 0
  passwordPrimary: string = ''
  passwordConfirm: string = ''
  passwordsMatch: boolean = false

  constructor(
    public activeModal: NgbActiveModal,
    private userService: UserService
  ) { }

  ngOnInit(): void {
  }

  onPasswordPrimaryChange(event: any) {
    const password = event.target.value
    this.passwordPrimary = password
    if (password.length > 0) {
      this.userService.passwordStrength(event.target.value).subscribe(result => {
        this.strength = parseInt(result)
      })
    } else {
      this.strength = 0
    }
  }

  onPasswordConfirmChange(event: any) {
    const password = event.target.value
    this.passwordConfirm = password
  }

  isModalValid() {
    return this.strength > 1 && this.passwordPrimary === this.passwordConfirm
  }

}
