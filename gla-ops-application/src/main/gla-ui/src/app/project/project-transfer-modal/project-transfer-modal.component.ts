import {Component, Input, OnInit} from '@angular/core';
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {isArray, map} from "lodash-es";
import {ProjectService} from "../project.service";
import {ToastrUtilService} from "../../shared/toastr/toastr-util.service";
declare var $: any;

@Component({
  selector: 'gla-project-transfer-modal',
  templateUrl: './project-transfer-modal.component.html',
  styleUrls: ['./project-transfer-modal.component.scss']
})
export class ProjectTransferModalComponent implements OnInit {
  @Input() projects: any[];
  project: any;
  isTransferAllowed = false;
  orgCode: string;
  transferred = false;
  nbTransferredMsg: string;
  nbErrorMsg: string;
  orgName: string;

  constructor(public activeModal: NgbActiveModal,
              private projectService: ProjectService,
              private toastrUtilService: ToastrUtilService ) { }

  ngOnInit() {
    let bulkTransfer = isArray(this.projects);
    if(!bulkTransfer){
      //TODO make it array on project overview
      this.projects = [this.projects];
      this.project = this.projects[0] || {};
      this.isTransferAllowed = (this.project.allowedActions || []).indexOf('Transfer') !== -1;
    } else {
      this.isTransferAllowed = true;
    }
  }

  onTransfer() {
    const ids = [];
    map(this.projects, (project) => {
      ids.push(project.id);
    });

    this.projectService.transferProject(ids, this.orgCode).subscribe((resp) => {
      this.transferred = true;

      let transferCount = resp as any;
      if (transferCount.nbTransferred) {
        this.nbTransferredMsg = transferCount.nbTransferred + ' project' + (transferCount.nbTransferred > 1 ? 's ' : ' ') + 'transferred';
      } else {
        this.nbTransferredMsg = '';
      }

      if (transferCount.nbErrors) {
        if (transferCount.nbTransferred > 0) {
          this.nbErrorMsg = transferCount.nbErrors +
            ' project' +
            (transferCount.nbErrors > 1 ? 's ' : ' ') +
            'not transferred';
        } else {
          this.nbErrorMsg = 'No projects transferred';
        }
      } else {
        this.nbErrorMsg = '';
      }

      if (transferCount.nbTransferred > 0) {
        this.toastrUtilService.success(this.nbTransferredMsg);
      } else {
        this.toastrUtilService.warning(this.nbErrorMsg);
      }
      $('#toast-container').css('z-index', '9999');
    }, (resp) => {
      this.nbErrorMsg = resp.error.description;
      this.transferred = true;
    });
  };
}
