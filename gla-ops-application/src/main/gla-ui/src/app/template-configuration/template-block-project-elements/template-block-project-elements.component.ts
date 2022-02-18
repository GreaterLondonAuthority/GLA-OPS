import { Component, OnInit, Input } from '@angular/core';

@Component({
  selector: 'gla-template-block-project-elements',
  templateUrl: './template-block-project-elements.component.html',
  styleUrls: ['./template-block-project-elements.component.scss']
})
export class TemplateBlockProjectElementsComponent implements OnInit {
  @Input() block
  @Input() template
  @Input() readOnly : boolean
  @Input() editable : boolean

  constructor() { }

  ngOnInit(): void {
  }

}
