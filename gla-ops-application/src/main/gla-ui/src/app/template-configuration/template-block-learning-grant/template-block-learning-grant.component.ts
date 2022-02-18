import {Component, Input, OnInit} from '@angular/core';
import {ReferenceDataService} from "../../reference-data/reference-data.service";
import {filter, find, merge} from "lodash-es";
import {TemplateService} from "../../template/template.service";
import {NavigationService} from "../../navigation/navigation.service";
import {ToastrUtilService} from "../../shared/toastr/toastr-util.service";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {LearningGrantLabelsModalComponent} from "./learning-grant-labels-modal/learning-grant-labels-modal.component";

@Component({
  selector: 'gla-template-block-learning-grant',
  templateUrl: './template-block-learning-grant.component.html',
  styleUrls: ['./template-block-learning-grant.component.scss']
})
export class TemplateBlockLearningGrantComponent implements OnInit {

  @Input() block
  @Input() template
  @Input() readOnly: boolean
  @Input() draft: boolean
  @Input() editable: boolean
  @Input() $state: any;
  allocationProfileOptions: { id: string; label: string }[]
  allocationTypesOptions: any
  editLabelsCommand: any
  errorMsg: any


  constructor(private referenceDataService: ReferenceDataService,
              private navigationService: NavigationService,
              private templateService: TemplateService,
              private toastrUtil: ToastrUtilService,
              private ngbModal: NgbModal) {
    this.$state = this.navigationService.getCurrentStateParams();
  }

  ngOnInit(): void {
    this.allocationProfileOptions = this.referenceDataService.getAllocationProfileOptions()
    this.allocationTypesOptions = this.referenceDataService.getAllocationTypesOptions()
  }

  onMultiSelectChange(check, options) {
    this.block.profileAllocationTypes = (options || []).filter(type => !!type.model).map(type => type.id);
  }

  onAllocationTypeMultiSelectChange(check, options) {
    this.block.allocationTypes = (options || []).filter(type => !!type.model).map(type => type.id);
  }

  showLearningGrantLabelsModal() {
    let modal = this.ngbModal.open(LearningGrantLabelsModalComponent);
    modal.componentInstance.block = this.block;
    modal.componentInstance.draft = this.draft
    modal.result.then((result) => {
      if (!this.draft) {
        this.performAction(result);
        merge(this.block, result)
      }
    }, () => {
    });
  }

  performAction(changes: any) {
    let labels = {
      profileTitle: changes.profileTitle,
      allocationTitle: changes.allocationTitle,
      cumulativeAllocationTitle: changes.cumulativeAllocationTitle,
      cumulativeEarningsTitle: changes.cumulativeEarningsTitle,
      cumulativePaymentTitle: changes.cumulativePaymentTitle,
      paymentDueTitle: changes.paymentDueTitle
    }
    let blockData = {
      blockId: this.block.id,
      learningGrantLabels: labels
    }
    this.editLabelsCommand = filter(this.block.templateBlockCommands, {name: 'EDIT_LEARNING_GRANT_LABELS'});
    this.editLabelsCommand.payload = {blockData}
    this.editLabelsCommand.displayOrder = this.block.displayOrder;
    this.editLabelsCommand.internalBlock = false;
    this.performCommand(this.editLabelsCommand, changes)
  }

  performCommand(command: any, changes: any) {
    this.templateService.performCommand(this.$state.templateId, command.internalBlock, command.displayOrder, command[0].name, command.payload || {}).toPromise().then((resp: any) => {
      this.toastrUtil.success(`Template updated`);

      this.errorMsg = null;
    }, (resp) => {
      this.errorMsg = resp.error ? resp.error.description : resp.data.description;
    });
  }


}
