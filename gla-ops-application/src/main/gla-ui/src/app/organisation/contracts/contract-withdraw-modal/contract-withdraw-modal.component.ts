import {Component, OnInit} from '@angular/core';
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";

@Component({
  selector: 'gla-contract-withdraw-modal',
  templateUrl: './contract-withdraw-modal.component.html',
  styleUrls: ['./contract-withdraw-modal.component.scss']
})

export class ContractWithdrawModalComponent implements OnInit {

  userComment: any

  constructor( public activeModal: NgbActiveModal) { }

  ngOnInit(): void {
  }

  isModalValid(){
    return this.userComment ? true : false;
  }

}
