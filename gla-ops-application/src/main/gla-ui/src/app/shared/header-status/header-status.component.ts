import {AfterViewInit, Component, ElementRef, OnInit, ViewChild, ViewEncapsulation} from '@angular/core';

@Component({
  selector: 'gla-header-status',
  templateUrl: './header-status.component.html',
  styleUrls: ['./header-status.component.scss'],
  encapsulation: ViewEncapsulation.None
})
export class HeaderStatusComponent implements OnInit, AfterViewInit {

  @ViewChild('hsCenter') hsCenter: ElementRef;

  hasCenter = true;
  sidesCls: any;

  constructor() {
  }

  ngOnInit(): void {
  }

  ngAfterViewInit() {
    this.hasCenter = !!(this.hsCenter && this.hsCenter.nativeElement.querySelector('hs-center'));
    this.sidesCls = {
      'col-sm-3': this.hasCenter,
      'col-sm-6': !this.hasCenter
    }
  }

}
