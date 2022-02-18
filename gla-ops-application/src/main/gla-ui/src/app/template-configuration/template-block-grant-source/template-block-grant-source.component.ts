import {Component, Input, OnInit} from '@angular/core';

@Component({
  selector: 'gla-template-block-grant-source',
  templateUrl: './template-block-grant-source.component.html',
  styleUrls: ['./template-block-grant-source.component.scss']
})
export class TemplateBlockGrantSourceComponent implements OnInit {

  @Input() block
  @Input() template
  @Input() readOnly : boolean
  @Input() editable : boolean
  grantTypesOptions : { id: string; label: string, model: boolean }[]

  constructor() { }

  ngOnInit(): void {
    let availableGrantTypes = [
      {
        id: 'Grant',
        label: 'Grant',
      }, {
        id: 'RCGF',
        label: 'Recycled Capital Grant Fund (RCGF)',
      }, {
        id: 'DPF',
        label: 'Disposal Proceeds Fund (DPF)',
      }
    ]

    this.grantTypesOptions = []
    availableGrantTypes.forEach(item => {
      this.grantTypesOptions.push({
        label: item.label,
        id: item.id,
        model: (this.block.grantTypes || []).includes(item.id)
      })
    })
  }

  onMultiSelectChange(check, options) {
    this.block.grantTypes = (options || []).filter(type => !!type.model).map(type => type.id);
  }

}
