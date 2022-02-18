import {Attribute, Component, EventEmitter, Input, OnInit, Optional, Output, ViewEncapsulation} from '@angular/core';

@Component({
  selector: 'gla-section-header',
  templateUrl: './section-header.component.html',
  styleUrls: ['./section-header.component.scss'],
  encapsulation: ViewEncapsulation.None
})
export class SectionHeaderComponent implements OnInit {

  @Input() subheader: string
  @Input()  level: number
  @Input()  collapsible: boolean
  @Input()  collapsed: boolean
  @Output() onCollapseChange = new EventEmitter<boolean>()

  constructor() { }

  ngOnInit(): void {
  }

  onSectionClick(){
    this.collapsed = !this.collapsed;
    this.onCollapseChange.emit(this.collapsed)
  }
}


// class SectionHeader {
//   constructor($element) {
//     this.$element = $element;
//   }
//
//   $onInit() {
//     this.isCollapsible = this.$element[0].hasAttribute('collapsed');
//   }
//
//
// }
//
// SectionHeader.$inject = ['$element'];
//
// gla.component('sectionHeader', {
//   templateUrl: 'scripts/components/section-header/section-header.html',
//   controller: SectionHeader,
//   bindings: {
//     subheader: '@',
//     level: '@',
//     collapsed: '=?',
//     onCollapseChange: '&'
//   },
//   transclude: true
// });


