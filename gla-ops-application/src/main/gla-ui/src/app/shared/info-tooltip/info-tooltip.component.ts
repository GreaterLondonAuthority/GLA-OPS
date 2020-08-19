import {Component, Input, OnInit} from '@angular/core';


@Component({
  selector: 'gla-info-tooltip',
  templateUrl: './info-tooltip.component.html',
  styleUrls: ['./info-tooltip.component.scss']
})
export class InfoTooltipComponent implements OnInit {

  @Input() helpText: string

  closeDelay(tooltip) {
    setTimeout(() => {
      tooltip.close();
    }, 3000);
  }

  constructor() { }

  ngOnInit(): void {
  }

}
