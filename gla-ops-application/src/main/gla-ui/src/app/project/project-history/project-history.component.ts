import {Component, Input, OnInit} from '@angular/core';
import {ProjectService} from "../project.service";
declare var moment: any;

@Component({
  selector: 'gla-project-history',
  templateUrl: './project-history.component.html',
  styleUrls: ['./project-history.component.scss']
})
export class ProjectHistoryComponent implements OnInit {

  @Input() items: any
  transitionMap: any;
  isPanelOpen = false;

  constructor(private projectService: ProjectService) { }

  ngOnInit(): void {
    this.transitionMap = this.projectService.getTransitionMap();
    //TODO is it still needed?
    // setTimeout(()=>{
    //   BootstrapUtil.setAriaDefaults();
    // }, 0);
  }

  onPanelChange(event){
    this.isPanelOpen = event.nextState;
  }

  formatDate(date) {
    return moment(date).format('DD/MM/YYYY [at] HH:mm');
  }
}
