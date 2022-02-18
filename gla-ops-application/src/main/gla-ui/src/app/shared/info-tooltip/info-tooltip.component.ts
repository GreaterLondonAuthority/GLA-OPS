import {Component, Input, OnInit} from '@angular/core';
import {NgbTooltip} from "@ng-bootstrap/ng-bootstrap";


@Component({
  selector: 'gla-info-tooltip',
  templateUrl: './info-tooltip.component.html',
  styleUrls: ['./info-tooltip.component.scss']
})
export class InfoTooltipComponent implements OnInit {

  @Input() helpText: string
  private focused: boolean;

  constructor() { }

  ngOnInit(): void {
    this.focused = false;
  }

  mouseEnter(t: NgbTooltip) {
    t.open();
  }

  mouseLeave(t: NgbTooltip) {
    t.close();
  }

  focusin(t: NgbTooltip) {
    this.focused = true;
    t.open();
  }

  focusout(t: NgbTooltip) {
    this.focused = false;
    //Close only if a new element was not focused after last blur
    setTimeout(() => {
      if(!this.focused){
        t.close();
      }
    });
  }
}
