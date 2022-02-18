import {Component, Input, OnInit, SimpleChanges} from '@angular/core';

@Component({
  selector: 'gla-project-overview-block',
  templateUrl: './project-overview-block.component.html',
  styleUrls: ['./project-overview-block.component.scss']
})
export class ProjectOverviewBlockComponent implements OnInit {
  @Input() block: any
  @Input() blockNumber: any
  @Input() projectStatus: any
  @Input() isLandProject = false
  complete: boolean;
  projectIsActive: boolean;
  projectIsClosed: boolean;
  isBlockUnapproved: boolean;
  isBlockApproved: boolean;
  icon: string;
  blockState: string;
  status: string;
  banner: string;

  constructor() { }

  ngOnInit(): void {

  }

  ngOnChanges(changes: SimpleChanges): void {
    //TODO might need deep check
    if (changes.block) {
      this.init();
    }
  }

  init(){
    this.complete = this.block.complete;
    this.projectIsActive = this.projectStatus === 'Active';
    this.projectIsClosed = this.projectStatus === 'Closed';
    this.isBlockUnapproved = !this.isLandProject && this.block.blockStatus === 'UNAPPROVED';
    this.isBlockApproved = !this.isBlockUnapproved;
    this.icon = this.getIcon();
    this.blockState = this.getBlockState();
    this.status = this.getStatus();
    this.banner = this.getBanner();
  }

  isActiveOrClosed() {
    return this.projectIsActive || this.projectIsClosed;
  }

  isGreenTheme() {
    return this.isActiveOrClosed() && this.isBlockApproved && !(this.isLandProject && !this.complete)
  }

  getBlockState() {
    return this.isGreenTheme() ? 'valid' : 'invalid'
  }

  getStatus() {
    if (!this.isActiveOrClosed()) {
      return this.complete ? 'COMPLETE' : 'INCOMPLETE';
    } else if (this.projectIsActive && this.isLandProject) {
      return null;
    }
    return this.isBlockUnapproved ? 'UNAPPROVED' : 'APPROVED'
  }

  getIcon() {
    if (!this.isActiveOrClosed() && this.complete || this.isGreenTheme()) {
      return 'glyphicon-ok';
    } else if (!this.isLandProject) {
      return 'glyphicon-exclamation-sign';
    }
  }

  getBanner() {
    return (this.isActiveOrClosed() && !this.complete) ? 'INCOMPLETE' : null
  }
}
