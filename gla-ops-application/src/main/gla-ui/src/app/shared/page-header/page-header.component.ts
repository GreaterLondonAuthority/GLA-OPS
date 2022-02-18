import {AfterViewInit, Component, ElementRef, EventEmitter, Input, OnInit, Output, ViewChild} from '@angular/core';
import {UserService} from "../../user/user.service";

@Component({
  selector: 'gla-page-header',
  templateUrl: './page-header.component.html',
  styleUrls: ['./page-header.component.scss']
})
export class PageHeaderComponent implements OnInit, AfterViewInit {

  @Input() editableBlock: any
  @Input() header: string
  @Input() backBtnName: string
  @Input() hideBackBtn: boolean
  @Input() stopEditing: string
  @Input() warning: string
  @Output() onBack = new EventEmitter<void>();
  @ViewChild('editCol') editCol: ElementRef;

  currentUser: any;
  hasButtons: boolean;

  constructor(private userService: UserService) { }

  ngOnInit(): void {
  }

  ngAfterViewInit(): void {
    let hasCustomRightSide = !!(this.editCol && this.editCol.nativeElement.querySelector('ph-right'));
    this.currentUser = (this.userService.currentUser() || {}).username;
    this.hasButtons = !this.hideBackBtn || this.editableBlock || hasCustomRightSide;
  }
}
