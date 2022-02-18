import {Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import {TemplateBlockMilestonesService} from "./template-block-milestones.service";
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {ProcessingRouteModalService} from "../processing-route-modal/processing-route-modal.service";
import {cloneDeep, filter, merge, remove} from "lodash-es";
import {ConfirmationDialogService} from "../../shared/confirmation-dialog/confirmation-dialog.service";
import {TemplateMilestoneModalComponent} from "../template-milestone-modal/template-milestone-modal.component";
import {ReferenceDataService} from "../../reference-data/reference-data.service";
import {TemplateService} from "../../template/template.service";
import {ToastrUtilService} from "../../shared/toastr/toastr-util.service";
import {NavigationService} from "../../navigation/navigation.service";


@Component({
  selector: 'gla-template-block-milestones',
  templateUrl: './template-block-milestones.component.html',
  styleUrls: ['./template-block-milestones.component.scss']
})

export class TemplateBlockMilestonesComponent implements OnInit, OnChanges {

  @Input() block
  @Input() template
  @Input() readOnly: boolean
  @Input() editable: boolean
  @Input() draft: boolean
  @Input() $state: any;

  applicabilityOptions: { id: string; label: string }[];
  showEvidenceOptions: { id: string; label: string }[];
  milestoneTypeOptions: { id: string; label: string }[];
  editMilestoneCommand: any
  errorMsg: any

  constructor(
    private templateBlockMilestonesService: TemplateBlockMilestonesService,
    private processingRouteModalService: ProcessingRouteModalService,
    private navigationService: NavigationService,
    private confirmationDialogService: ConfirmationDialogService,
    private referenceDataService: ReferenceDataService,
    private templateService: TemplateService,
    private toastrUtil: ToastrUtilService,
    private modalService: NgbModal
  ) {
    this.$state = this.navigationService.getCurrentStateParams();
  }

  ngOnInit() {
    this.applicabilityOptions = this.templateBlockMilestonesService.getApplicabilityOptions();
    this.showEvidenceOptions = this.templateBlockMilestonesService.getShowEvidenceOptions();
    this.milestoneTypeOptions = this.templateBlockMilestonesService.getMilestoneTypeOptions();
    if(!this.block.processingRoutes || !this.block.processingRoutes.length){
      this.block.processingRoutes = [{
        name: 'default',
        displayOrder: 1,
        milestones: []
      }];
    }
  }

  ngOnChanges(changes: SimpleChanges): void {
    this.refreshBlockDataDisplay()
  }

  refreshBlockDataDisplay(){
    this.templateBlockMilestonesService.sortProcessingRoutesAndMilestones(this.block)
  }

  showProcessingRouteModal(processingRoute?: any) {
    let isNew = !processingRoute || !processingRoute.name
    let modal = this.processingRouteModalService.show(cloneDeep(processingRoute), this.block.processingRoutes);
    modal.result.then((result) => {
      if (isNew) {
        this.block.processingRoutes.push(result);
        this.templateBlockMilestonesService.sortProcessingRoutesAndMilestones(this.block);
      } else{
        merge(processingRoute, result)
      }
    }, ()=>{});
  }

  deleteProcessingRoute(processingRoute){
    let modal =
      this.confirmationDialogService.delete(
        `Are you sure you want to delete ${processingRoute.name}?<br/>Deleting this Processing Route will also remove any preset milestones?`);
    modal.result.then(()=>{
      remove(this.block.processingRoutes, processingRoute);
    });
  }

  updateProcessingRoute(processingRoutes){
    this.block.processingRoutes = processingRoutes;
    this.refreshBlockDataDisplay();
  }

  updateMilestones( milestones, processingRoute){
    let currentIndex = this.block.processingRoutes.indexOf(processingRoute);
    this.block.processingRoutes[currentIndex].milestones = milestones
    this.refreshBlockDataDisplay();
  }

  showMilestoneModal(processingRoute: any, milestone: any) {
    let isNew = !milestone || !milestone.summary
    let modal = this.modalService.open(TemplateMilestoneModalComponent)
    modal.componentInstance.milestone = cloneDeep(milestone || {});
    modal.componentInstance.milestones = processingRoute.milestones || [];
    modal.componentInstance.milestoneType = this.template.milestoneType;
    modal.componentInstance.draft = this.draft;
    modal.result.then((result) => {
      if (isNew) {
        processingRoute.milestones = processingRoute.milestones || [];
        processingRoute.milestones.push(result);
      }
      else if (!this.draft) {
        this.performAction(processingRoute, milestone, result);
      } else {
        merge(milestone, result)
      }
      this.refreshBlockDataDisplay();
    }, ()=>{});
  }

  performAction(processingRoute :any, milestone: any, changes: any) {
    let blockData = {
      blockId: this.block.id,
      processingRouteId: processingRoute.externalId,
      milestoneExternalId: milestone.externalId,
      milestoneSummary: milestone.summary,
      milestoneDisplayOrder: changes.displayOrder,
      milestoneRequirement: changes.requirement,
      milestoneNaSelectable: changes.naSelectable
    }
    this.editMilestoneCommand = filter(this.block.templateBlockCommands, {name: 'EDIT_MILESTONES'});
    this.editMilestoneCommand.payload = { blockData}
    this.editMilestoneCommand.displayOrder = this.block.displayOrder;
    this.editMilestoneCommand.internalBlock = false;
    this.performCommand(this.editMilestoneCommand, milestone, changes)
  }

  performCommand(command: any, milestone: any, changes: any) {
    this.templateService.performCommand(this.$state.templateId, command.internalBlock, command.displayOrder, command[0].name, command.payload || {}).toPromise().then((resp:any) => {
      merge(milestone, changes);
      this.toastrUtil.success(`Template updated`);
      this.errorMsg = null;
    }, (resp) => {
      this.errorMsg = resp.error? resp.error.description : resp.data.description;
    });
  }


  deleteMilestone(processingRoute, milestone){
    let modal = this.confirmationDialogService.delete(`Are you sure you want to delete ${milestone.summary}?`);
    modal.result.then(()=>{
      remove(processingRoute.milestones, milestone);
    });
  }

  onMilestoneTypeChange(milestoneType: string) {
    if(milestoneType !== 'MonetarySplit'){
      this.block.processingRoutes.forEach(pr => {
        (pr.milestones || []).forEach(m => {
          m.monetarySplit = null;
          if(milestoneType === 'NonMonetary'){
            m.monetary = false;
          }
        });
      });
    }
  }
}
