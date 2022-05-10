import {Component, Input, OnInit} from '@angular/core';

@Component({
  selector: 'gla-tile',
  templateUrl: './tile.component.html',
  styleUrls: ['./tile.component.scss']
})
export class TileComponent implements OnInit {

  @Input() items: any;
  @Input() hlevel: any;

  constructor() { }

  ngOnInit(): void {
  }

}
