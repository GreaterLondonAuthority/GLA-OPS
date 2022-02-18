import {Component, Input, OnInit} from '@angular/core';

@Component({
  selector: 'gla-loading-mask',
  templateUrl: './loading-mask.component.html',
  styleUrls: ['./loading-mask.component.scss']
})
export class LoadingMaskComponent implements OnInit {
  @Input()
  text: string;

  constructor() { }

  ngOnInit(): void {
  }

}
