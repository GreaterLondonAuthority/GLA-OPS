import {Component, Input, OnInit} from '@angular/core';
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";

@Component({
  selector: 'gla-payment-authorisation-modal',
  templateUrl: './payment-authorisation-modal.component.html',
  styleUrls: ['./payment-authorisation-modal.component.scss']
})
export class PaymentAuthorisationModalComponent implements OnInit {

  @Input() paymentGroup: any

  constructor(public activeModal: NgbActiveModal) { }

  ngOnInit(): void {
  }

}
