import { Component, Input, OnInit } from '@angular/core';
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";

@Component({
  selector: 'gla-delete-file-modal',
  templateUrl: './delete-file-modal.component.html',
  styleUrls: ['./delete-file-modal.component.scss']
})
export class DeleteFileModalComponent implements OnInit {
  @Input() file: any
  @Input() title: string
  @Input() message: string
  @Input() confirmationText: string = 'YES'
  @Input() cancelText: string = 'NO'

  constructor(public activeModal: NgbActiveModal) { }

  ngOnInit(): void {
  }

}
