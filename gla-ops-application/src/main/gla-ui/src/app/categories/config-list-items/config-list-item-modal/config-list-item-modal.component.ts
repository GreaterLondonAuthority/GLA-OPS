import {Component, Input, OnInit} from '@angular/core';
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {NgForm} from "@angular/forms";
import {ReferenceDataService} from "../../../reference-data/reference-data.service";
import {cloneDeep} from "lodash-es";

@Component({
  selector: 'gla-config-list-item-modal',
  templateUrl: './config-list-item-modal.component.html',
  styleUrls: ['./config-list-item-modal.component.scss']
})
export class ConfigListItemModalComponent implements OnInit {

  @Input() availableExternalIds: any[];
  @Input() listItem;

  showGroupIdInputBox: boolean;
  errorMessage: string;
  isGroupIdDuplicate: boolean;

  constructor(public activeModal: NgbActiveModal, public referenceDataService : ReferenceDataService) { }

  ngOnInit(): void {
    this.listItem = cloneDeep(this.listItem);
  }

  updateMode(): boolean {
    return this.listItem.id;
  }

  isFormValid(modalForm: NgForm) {
    return modalForm.form.valid && !this.isGroupIdDuplicate;
  }

  onUpdate(item) {
    this.referenceDataService.updateConfigItem(item).subscribe( () => {
      this.activeModal.close(item);
    },(error) => {
      this.errorMessage = error.error.description;
    } );
  }

  onCreate(item) {
    if (!item.externalId) {
      item.externalId = item.chosenExternalId;
    }
    this.referenceDataService.createConfigItem(item).subscribe( () => {
      this.activeModal.close(item);
    },(error) => {
      this.errorMessage = error.error.description;
    } );
  }

  toggleShowInput(value) {
    this.listItem.externalId = '';
    this.showGroupIdInputBox =value;
  }

  validateGroupId() {
    this.isGroupIdDuplicate = this.availableExternalIds.indexOf(+this.listItem.externalId) > -1;
  }
}
