import {Component, Input, OnInit} from '@angular/core';
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {OutputCategoryService} from "../output-category.service";

@Component({
  selector: 'gla-output-category-modal',
  templateUrl: './output-category-modal.component.html',
  styleUrls: ['./output-category-modal.component.scss']
})
export class OutputCategoryModalComponent implements OnInit {

  @Input() $stateParams: any;
  @Input() model: any = {}
  errorMessage: '';

  constructor(public activeModal: NgbActiveModal, private outputCatService : OutputCategoryService) {
  }

  ngOnInit(): void {
  }

  getValueTypes() {
    return [
      'UNITS',
      'HECTARES',
      'POSITIONS',
      'SQUARE_METRES',
      'SQUARE_METRES_NET',
      'SQUARE_METRES_GROSS',
      'BEDROOMS',
      'MONETARY_VALUE',
      'NUMBER_OF',
      'NUMBER_OF_DECIMAL',
      'ENTER_VALUE',
      'ENTER_VALUE_DECIMALS',
      'NET_AREA',
      'DISTANCE',
      'LENGTH',
      'OUTPUTS',
      'RESULTS'];
  }

  isFormValid() {
    return this.model.id && this.model.category && this.model.subcategory && this.model.valueType;
  }

  createOrSave() {
      this.outputCatService.createOutputConfigurationGroup(this.model).subscribe( () => {
        this.activeModal.close(this.model);
      },(error) => {
        this.errorMessage=error.error.description;
      } );
  }
}
