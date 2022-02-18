import { Component, OnInit, Input } from '@angular/core';

@Component({
  selector: 'gla-template-block-project-objectives',
  templateUrl: './template-block-project-objectives.component.html',
  styleUrls: ['./template-block-project-objectives.component.scss']
})
export class TemplateBlockProjectObjectivesComponent implements OnInit {
  @Input() block
  @Input() template
  @Input() readOnly : boolean
  @Input() editable : boolean

  constructor() { }

  ngOnInit(): void {
  }
}
