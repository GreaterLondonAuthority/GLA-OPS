import {Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import {TemplateBlockMilestonesService} from "./template-block-milestones.service";
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {ProcessingRouteModalService} from "../processing-route-modal/processing-route-modal.service";
import {cloneDeep, merge, remove} from "lodash-es";
import {ConfirmationDialogService} from "../../shared/confirmation-dialog/confirmation-dialog.service";
import {TemplateMilestoneModalComponent} from "../template-milestone-modal/template-milestone-modal.component";
import {ReferenceDataService} from "../../reference-data/reference-data.service";


@Component({
  selector: 'template-block-milestones',
  templateUrl: './template-block-milestones.component.html',
  styleUrls: ['./template-block-milestones.component.scss']
})

export class TemplateBlockMilestonesComponent implements OnInit, OnChanges {

  @Input() block
  @Input() template
  @Input() readOnly: boolean
  applicabilityOptions: { id: string; label: string }[];
  milestoneTypeOptions: { id: string; label: string }[];

  constructor(
    private templateBlockMilestonesService: TemplateBlockMilestonesService,
    private processingRouteModalService: ProcessingRouteModalService,
    private confirmationDialogService: ConfirmationDialogService,
    private referenceDataService: ReferenceDataService,
    private modalService: NgbModal
  ) {
  }

  ngOnInit() {
    this.applicabilityOptions = this.templateBlockMilestonesService.getApplicabilityOptions();
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
    let modal = this.confirmationDialogService.delete(`Are you sure you want to delete ${processingRoute.name}?`);
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
    modal.result.then((result) => {
      if (isNew) {
        processingRoute.milestones = processingRoute.milestones || [];
        processingRoute.milestones.push(result);
      } else{
        merge(milestone, result)
      }
    }, ()=>{});
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
