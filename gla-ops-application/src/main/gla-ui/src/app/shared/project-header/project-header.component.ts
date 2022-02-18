import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {NavigationService} from "../../navigation/navigation.service";

@Component({
  selector: 'gla-project-header',
  templateUrl: './project-header.component.html',
  styleUrls: ['./project-header.component.scss']
})
export class ProjectHeaderComponent implements OnInit {

  @Input() backBtnName;
  @Input() stopEditing;
  @Input() editableBlock;
  @Input() subtitle;
  @Input() project;
  @Input() linkMenuItems;
  @Input() actionMenuItems;
  @Input() createBtnName;
  @Input() showUkprn;
  @Input() loading;
  @Output() onBack = new EventEmitter<any>();
  @Output() onActionClicked = new EventEmitter<any>();
  @Output() onCreate = new EventEmitter<any>();


  undoLinkText: string;

  constructor(private navigationService: NavigationService) { }

  ngOnInit(): void {
    this.undoLinkText = 'Undo unapproved changes';
  }

  get hasVersion(){
    return this.editableBlock && this.editableBlock.approvalTime
  }

  goTo(stateName, stateParams){
    this.navigationService.goToUiRouterState(stateName, stateParams, null);
  }
}
