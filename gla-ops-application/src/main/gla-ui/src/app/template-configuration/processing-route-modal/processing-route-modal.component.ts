import {Component, Input, OnInit} from '@angular/core';
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {NgForm} from "@angular/forms";
import {find} from "lodash-es";


@Component({
  selector: 'processing-route-modal',
  templateUrl: './processing-route-modal.component.html',
  styleUrls: ['./processing-route-modal.component.scss']
})
export class ProcessingRouteModalComponent implements OnInit {

  @Input() processingRoute: any
  @Input() processingRoutes: any
  originalProcessingRoute: any

  constructor(public activeModal: NgbActiveModal) {
  }

  ngOnInit(): void {
    this.processingRoute = this.processingRoute || {};
    this.originalProcessingRoute = find(this.processingRoutes, {
      name: this.processingRoute.name
    });
  }

  isNameUnique(processingRoute) {
    return !this.processingRoutes.find(pr => pr.name === processingRoute.name && pr != this.originalProcessingRoute);
  }

  isDisplayOrderUnique(processingRoute) {
    return !this.processingRoutes.find(pr => pr.displayOrder === processingRoute.displayOrder && pr != this.originalProcessingRoute);
  }

  isFormValid(modalForm: NgForm, processingRoute) {
    return modalForm.form.valid && this.isNameUnique(processingRoute) && this.isDisplayOrderUnique(processingRoute)
  }
}
