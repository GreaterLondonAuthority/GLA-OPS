import {Component, Input, OnInit} from '@angular/core';

@Component({
  selector: 'gla-overview-block',
  templateUrl: './overview-block.component.html',
  styleUrls: ['./overview-block.component.scss']
})
export class OverviewBlockComponent implements OnInit {
  @Input() number: number
  @Input() name: string
  @Input() status: string
  @Input() banner: string
  @Input() icon: string
  @Input() blockState: any
  @Input() newBlock: boolean
  @Input() color: string


  constructor() {
    // this.number = 1;
    // this.name = 'NAME';
    // this.status = 'STATUS';
    // this.banner = 'BANNER';
    // this.icon = 'glyphicon-ok';
    // this.state='valid';
    // this.newBlock=true;
  }

  ngOnInit(): void {
  }

}
