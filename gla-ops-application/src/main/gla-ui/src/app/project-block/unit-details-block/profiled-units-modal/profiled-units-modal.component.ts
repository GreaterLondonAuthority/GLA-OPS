import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {UnitsService} from "../units.service";
import {clone, cloneDeep, filter, forEach, isNil} from "lodash-es";

@Component({
  selector: 'gla-profiled-units-modal',
  templateUrl: './profiled-units-modal.component.html',
  styleUrls: ['./profiled-units-modal.component.scss']
})
export class ProfiledUnitsModalComponent implements OnInit {

  @Input() type: string
  @Input() config: any
  @Input() showMarketTypes = false

  title: string;
  unit: any;
  tenureDetails: any[];

  constructor(public activeModal: NgbActiveModal,
              public unitsService: UnitsService) { }

  ngOnInit(): void {
    this.title = (this.type === 'Sales'? 'Add Sale Details' : 'Add Rental Details');

    this.unit = {
      type: this.type || 'Rent',
      marketType: null
    };

    let tenureTypesFilter = {'availableForRental': true} as any;
    if(this.type === 'Sales'){
      tenureTypesFilter = {'availableForSales': true};
    }

    let tenureDetails = [];
    forEach(this.config.tenureDetails, (tenure) => {
      let tenureClone = clone(tenure);
      tenureClone.marketTypes = filter(tenureClone.marketTypes, tenureTypesFilter);
      if(tenureClone.marketTypes.length > 0){
        tenureDetails.push(tenureClone);
      }
    });

    this.tenureDetails = tenureDetails;
    if(tenureDetails.length === 1){
      this.unit.tenure = tenureDetails[0];
      this.onTenureSelect();
    }

    console.log('component config:', this.tenureDetails);
  }

  onTenureSelect() {
    if(this.unit.tenure.marketTypes.length === 1) {
      this.unit.marketType = this.unit.tenure.marketTypes[0];
    } else {
      this.unit.marketType = null;
    }
    this.onMarketTypeSelect();
  }

  onMarketTypeSelect() {
    if(this.unit.marketType) {
      if(this.unit.type === 'Rent' && this.unit.marketType.id !== this.unitsService.LEGACY_RENT_MARKET_TYPE_ID){
        this.unit.weeklyMarketRent = undefined;
      }
      if(this.unit.type === 'Sales' && this.unit.marketType.id === this.unitsService.DISCOUNTED_RATE_MARKET_TYPE_ID){
        this.unit.firstTrancheSales = undefined;
      }
      if(this.unit.type === 'Sales' && this.unit.marketType.id !== this.unitsService.DISCOUNTED_RATE_MARKET_TYPE_ID){
        this.unit.discountOffMarketValue = undefined;
      }
    } else {
      this.unit.weeklyMarketRent = undefined;
      this.unit.firstTrancheSales = undefined;
      this.unit.discountOffMarketValue = undefined;
    }
  }

  isSharedOwnership() {
    return this.unit?.tenure?.name.toLowerCase().includes('shared ownership');
  }

  isValidForm(){
    let requiredFields, firstTrancheValid = true, isDiscountValid = true, isRentValid = true;
    if(this.unit.type === 'Sales') {
      requiredFields = ['tenure', 'marketType', 'nbBeds', 'unitType', 'nbUnits', 'marketValue', 'firstTrancheSales', 'weeklyServiceCharge'];
      firstTrancheValid = !this.isPercentageInvalid(this.unit.firstTrancheSales)
      if (this.unit.marketType && this.unit.marketType.id === this.unitsService.DISCOUNTED_RATE_MARKET_TYPE_ID) {
        //'firstTrancheSales' is replaced with 'discountOffMarketValue' for this market type
        requiredFields[6] = 'discountOffMarketValue';
        isDiscountValid = !this.isPercentageInvalid(this.unit.discountOffMarketValue)
      } else if (this.unit.marketType && this.unit.marketType.id === this.unitsService.LEGACY_SALES_MARKET_TYPE_ID) {
        requiredFields.push('netWeeklyRent');
      }
      if (this.isSharedOwnership()) {
        requiredFields.push('rentChargedOnUnsoldEquity');
        isRentValid = !this.isPercentageInvalid(this.unit.rentChargedOnUnsoldEquity)
      }
    } else {
      requiredFields = ['tenure', 'marketType', 'nbBeds', 'unitType', 'nbUnits', 'netWeeklyRent', 'weeklyServiceCharge'];
      if(this.unit.marketType && this.unit.marketType.id === this.unitsService.LEGACY_RENT_MARKET_TYPE_ID){
        requiredFields.push('weeklyMarketRent');
      }
    }
    let hasMissingFields = requiredFields.some(requiredField => isNil(this.unit[requiredField]));
    return !hasMissingFields && firstTrancheValid && isDiscountValid && isRentValid;
  }

  isPercentageInvalid(percentage: number) {
    return percentage > 100;
  }

  add() {
    let data = cloneDeep(this.unit);
    data.tenureId = this.unit.tenure.externalId;
    delete data.tenure;
    this.activeModal.close(data)
  }
}
