import {Component, Input, OnInit} from '@angular/core';
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {NgForm} from "@angular/forms";
import {ReferenceDataService} from "../../reference-data/reference-data.service";
import {find} from "lodash-es";
import {TemplateBlockMilestonesService} from "../template-block-milestones/template-block-milestones.service";

@Component({
  selector: 'app-template-milestone-modal',
  templateUrl: './template-milestone-modal.component.html',
  styleUrls: ['./template-milestone-modal.component.scss']
})
export class TemplateMilestoneModalComponent implements OnInit {

  @Input() milestone: any
  @Input() milestones: any
  @Input() milestoneType: any
  originalMilestone: any
  requirementOptions: { id: string; label: string }[];
  isBlockMonetary: boolean;
  isBlockMonetarySplit: boolean;

  constructor(public activeModal: NgbActiveModal,
              private referenceDataService: ReferenceDataService,
              private templateBlockMilestonesService: TemplateBlockMilestonesService) { }


  ngOnInit(): void {
    this.originalMilestone = find(this.milestones, {
      summary: this.milestone.summary
    });
    this.requirementOptions = this.referenceDataService.getRequirementOptions().filter(ro => ro.id != 'hidden')
    this.isBlockMonetary = this.templateBlockMilestonesService.isMonetary(this.milestoneType);
    this.isBlockMonetarySplit = this.templateBlockMilestonesService.isMonetarySplit(this.milestoneType);
    if(!this.isBlockMonetary){
      this.milestone.monetary = false;
    }
  }

  onMonetaryChange(isMonetary){
    if(!isMonetary){
      this.milestone.monetarySplit = null;
    }
  }

  isNameUnique(milestone) {
    return !this.milestones.find(m => m.summary === milestone.summary && m != this.originalMilestone);
  }

  isDisplayOrderUnique(milestone) {
    return !this.milestones.find(m => m.displayOrder === milestone.displayOrder && m != this.originalMilestone);
  }

  isFormValid(modalForm: NgForm, milestone) {
    return modalForm.form.valid &&
      this.isNameUnique(milestone) &&
      this.isDisplayOrderUnique(milestone) &&
      this.milestone.keyEvent != null &&
      (!this.isBlockMonetary || this.milestone.monetary != null) &&
      this.milestone.naSelectable != null
  }
}
