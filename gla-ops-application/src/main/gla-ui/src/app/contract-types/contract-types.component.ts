import {Component, Input, OnInit} from '@angular/core';
import {OrganisationService} from "../organisation/organisation.service";

@Component({
  selector: 'gla-contract-types',
  templateUrl: './contract-types.component.html',
  styleUrls: ['./contract-types.component.scss']
})
export class ContractTypesComponent implements OnInit {

  contracts ;

  constructor(private organisationService: OrganisationService) {

  }

  ngOnInit(): void {
    this.contracts = this.organisationService.getContracts().subscribe((response) => {
      this.contracts = response
    }, (error) => {
      this.contracts = []
    })

  }

}
