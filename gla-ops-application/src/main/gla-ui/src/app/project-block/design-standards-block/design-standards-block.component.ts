import {Component, Injector, OnInit} from '@angular/core';
import {ProjectBlockComponent} from "../project-block.component";

@Component({
  selector: 'gla-design-standards-block',
  templateUrl: './design-standards-block.component.html',
  styleUrls: ['./design-standards-block.component.scss']
})
export class DesignStandardsBlockComponent extends ProjectBlockComponent {

  constructor(injector: Injector) {
    super(injector);
  }

  ngOnInit(): void {
    super.ngOnInit();
  }

  submit() {
    this.projectBlock.type = 'DesignStandardsBlock';
    if (this.projectBlock.meetingLondonHousingDesignGuide) {
      this.projectBlock.reasonForNotMeetingDesignGuide = null;
    }
    return this.projectBlockService.updateBlock(this.project.id, this.projectBlock.id, this.projectBlock, true);
  };
}
