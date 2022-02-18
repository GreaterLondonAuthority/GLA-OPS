import { Component, OnInit } from '@angular/core';
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";

@Component({
  selector: 'gla-mobile-device-warning',
  templateUrl: './mobile-device-warning.component.html',
  styleUrls: ['./mobile-device-warning.component.scss']
})
export class MobileDeviceWarningComponent implements OnInit {

  constructor(public activeModal: NgbActiveModal) { }

  ngOnInit(): void {
  }

}
