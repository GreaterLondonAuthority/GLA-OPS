import {Component, Input, OnInit} from '@angular/core';
import {filter, some, sortBy} from "lodash-es";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";

@Component({
  selector: 'gla-project-label-modal',
  templateUrl: './project-label-modal.component.html',
  styleUrls: ['./project-label-modal.component.scss']
})
export class ProjectLabelModalComponent implements OnInit {
  @Input() explanatoryText: string
  @Input() existingLabels: any[]
  @Input() preSetLabels: any[]
  label: any;
  labelTypes: any[];
  activePreSetLabels: any[];
  isExistingLabel = false;

  constructor(public activeModal: NgbActiveModal) { }

  ngOnInit(): void {
    this.explanatoryText = this.explanatoryText || 'Specify ad-hoc label text'
    this.label = {};
    this.labelTypes = [{
      labelName: 'Ad-hoc label',
      type: 'Custom'
    }, {
      labelName: 'Pre-set label',
      type: 'Predefined'
    }];
    this.activePreSetLabels = filter(this.preSetLabels, {status: 'Active'});
    this.activePreSetLabels = sortBy(this.activePreSetLabels, 'labelName');
    console.log('sorted', this.activePreSetLabels)
  }

  validate() {
    this.isExistingLabel = some(this.existingLabels, (label) =>{
      return (label.text || '').toLowerCase() === (this.label.text || '').toLowerCase();
    });
  }

  onLabelTypeChange () {
    if(this.label.type === 'Custom') {
      this.label.preSetLabel = null;
    } else {
      this.label.text = null;
    }
  }

  apply(){
    (this.label.preSetLabel || {}).managingOrganisation = {
      id: (this.label.preSetLabel || {}).managingOrganisationId
    };
    this.activeModal.close(this.label)
  }
}
