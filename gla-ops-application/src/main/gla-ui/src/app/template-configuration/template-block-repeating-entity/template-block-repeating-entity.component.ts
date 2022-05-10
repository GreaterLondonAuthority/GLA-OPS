import {Component, Input, OnInit} from '@angular/core';

@Component({
  selector: 'gla-template-block-repeating-entity',
  templateUrl: './template-block-repeating-entity.component.html',
  styleUrls: ['./template-block-repeating-entity.component.scss']
})
export class TemplateBlockRepeatingEntityComponent implements OnInit {

  @Input() block
  @Input() template
  @Input() readOnly: boolean
  @Input() draft: boolean
  @Input() editable: boolean
  @Input() blockEntity: string

  constructor() { }

  ngOnInit(): void {
    this.blockEntity = this.blockEntity ?  this.blockEntity : 'entities';
  }

}
