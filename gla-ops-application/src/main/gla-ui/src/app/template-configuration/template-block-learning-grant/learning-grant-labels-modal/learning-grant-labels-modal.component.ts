import {Component, Input, OnInit} from '@angular/core';
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {cloneDeep} from "lodash-es";

@Component({
  selector: 'gla-learning-grant-labels-modal',
  templateUrl: './learning-grant-labels-modal.component.html',
  styleUrls: ['./learning-grant-labels-modal.component.scss']
})
export class LearningGrantLabelsModalComponent implements OnInit {

  @Input() block: any
  @Input() draft: boolean
  originalBlock: any

  constructor(public activeModal: NgbActiveModal) { }

  ngOnInit(): void {
    this.originalBlock = this.block;
    this.block = cloneDeep(this.block);
  }

  addButtonEnabled() {
    return this.block.profileTitle;
  }

  changeLabels() {
    this.activeModal.close(this.block);
  }

}
