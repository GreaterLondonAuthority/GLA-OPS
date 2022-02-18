import {Component, Input, OnInit} from '@angular/core';
import {ConfigListItemModalComponent} from "./config-list-item-modal/config-list-item-modal.component";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {ToastrUtilService} from "../../shared/toastr/toastr-util.service";
import {ReferenceDataService} from "../../reference-data/reference-data.service";
import {ConfirmationDialogService} from "../../shared/confirmation-dialog/confirmation-dialog.service";
import {SessionService} from "../../session/session.service";

@Component({
  selector: 'gla-config-list-items',
  templateUrl: './config-list-items.component.html',
  styleUrls: ['./config-list-items.component.scss']
})
export class ConfigListItemsComponent implements OnInit {
  @Input() budgetCategories: any;
  @Input() state: any;
  @Input() stateParams: any;
  externalIds: string[];
  blockSessionStorage: any;

  constructor(private ngbModal: NgbModal,
              private toastrUtil: ToastrUtilService,
              private confirmationDialog: ConfirmationDialogService,
              public referenceDataService : ReferenceDataService,
              public sessionService : SessionService) { }

  ngOnInit(): void {
    this.externalIds = this.budgetCategories.map((item) => item.externalId);
    this.blockSessionStorage = this.sessionService.getConfigListItemsPage();
    if(!this.blockSessionStorage){
      this.sessionService.setConfigListItemsPage({toggleIcons: {}});
      this.blockSessionStorage = this.sessionService.getConfigListItemsPage();
    }
  }

  toggle(category: any) {
    category.expanded = !category.expanded
    this.sessionService.setConfigListItemsPage(this.blockSessionStorage);
  }

  addRow() {
    let modal = this.ngbModal.open(ConfigListItemModalComponent, { size: 'md' });
    modal.componentInstance.listItem = {};
    modal.componentInstance.availableExternalIds = this.externalIds;
    modal.result.then((data) => {
      this.toastrUtil.success('New row added');
      this.referenceDataService.getConfigItemsByType('BudgetCategories', {}).subscribe((rsp : any) => {
        this.budgetCategories = rsp;
      });
    }, () => {});
  }

  editRow(row) {
    let modal = this.ngbModal.open(ConfigListItemModalComponent, { size: 'md' });
    modal.componentInstance.listItem = row;
    modal.result.then((data) => {
      this.toastrUtil.success('Row updated');
      this.refreshPage();
    }, () => {});
  }

  refreshPage() {
    this.referenceDataService.getConfigItemsByType('BudgetCategories', {}).subscribe((rsp : any) => {
      this.budgetCategories = rsp;
    });
  }

  canDeleteFromGroup(usage: any[]) {
    return !(usage.some((item) => item.templateStatus === 'Active'));
  }

  deleteCategory(externalId: any, id?: any) {
    let group = this.budgetCategories.filter( (item) => item.externalId === externalId)[0];
    let item = group.categories.filter( (item) => item.id === id)[0];
    let message = 'Are you sure you want to delete \'' + item.category +  '\'?'
    if (group.categories.length === 1) {
      message += '<br/>Deleting this category will also remove group \'' + externalId + '\' ';
    }

    let modal = this.confirmationDialog.show({
      message: message,
      approveText: 'DELETE',
      dismissText: 'KEEP'
    });

    modal.result.then(() => {
      this.referenceDataService.deleteConfigItem(externalId, id).subscribe(() => {
        this.refreshPage();

      }, (error) => {
        this.toastrUtil.error(error.error);
      });
    }, () => {});
  }

  deleteGroup(externalId: any) {
    let message = 'Are you sure you want to delete group \'' + externalId +  '\'?' +
      '<br/> Deleting this group id will also remove any categories.'

    let modal = this.confirmationDialog.show({
      message: message,
      approveText: 'DELETE',
      dismissText: 'KEEP'
    });

    modal.result.then(() => {
      this.referenceDataService.deleteConfigGroup(externalId).subscribe(() => {
        this.refreshPage();

      }, (error) => {
        this.toastrUtil.error(error.error);
      });
    }, () => {});
  }
}
