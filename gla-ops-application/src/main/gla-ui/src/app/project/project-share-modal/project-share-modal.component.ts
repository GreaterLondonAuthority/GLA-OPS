import {Component, Input, OnInit} from '@angular/core';
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";

@Component({
  selector: 'gla-project-share-modal',
  templateUrl: './project-share-modal.component.html',
  styleUrls: ['./project-share-modal.component.scss']
})
export class ProjectShareModalComponent implements OnInit {
  @Input() projectId: number;
  orgName: string
  orgCode: string

  constructor(public activeModal: NgbActiveModal) { }

  ngOnInit(): void {
  }

}
