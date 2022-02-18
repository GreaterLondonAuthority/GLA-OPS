import {Component, Input, OnInit} from '@angular/core';
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";

@Component({
  selector: 'app-confirmation-dialog',
  templateUrl: './confirmation-dialog.component.html',
  styleUrls: ['./confirmation-dialog.component.scss']
})
export class ConfirmationDialogComponent implements OnInit {

  @Input() config: any;
  confirmed: false;

  userComment: any;
  constructor(public activeModal: NgbActiveModal) { }

  ngOnInit(): void {
    this.userComment = this.config.existingComment
  }

  isModalValid(userCommentRequired, userComment){
    return (!userCommentRequired || userComment != this.config.existingComment)
      && (!this.config.showConfirmationCheckbox || this.confirmed)
  }
}
