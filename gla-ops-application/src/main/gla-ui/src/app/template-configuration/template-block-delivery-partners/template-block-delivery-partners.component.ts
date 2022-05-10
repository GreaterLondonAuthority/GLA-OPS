import {Component, Input, OnInit} from '@angular/core';

@Component({
  selector: 'gla-template-block-delivery-partners',
  templateUrl: './template-block-delivery-partners.component.html',
  styleUrls: ['./template-block-delivery-partners.component.scss']
})
export class TemplateBlockDeliveryPartnersComponent implements OnInit {

  @Input() block
  @Input() template
  @Input() readOnly : boolean
  @Input() editable : boolean
  deliveryPartnerTypes: { id: string; label: string }[];
  deliverableTypes: { id: string; label: string, model: boolean }[]

  constructor() { }

  ngOnInit(): void {
    this.deliveryPartnerTypes = [
      {
        id: 'LearningProvider',
        label: 'Learning Provider',
      }, {
        id: 'Partner',
        label: 'Partner',
      }, {
        id: 'Other',
        label: 'Other',
      }
    ]

    let availableDeliverableTypes = [
      {
        id: 'ADULT_EDUCATION_BUDGET',
        label: 'Adult Education Budget (AEB)',
      }, {
        id: 'COMMUNITY_LEARNERS',
        label: 'Community Learners (CL)',
      }, {
        id: 'OTHER',
        label: 'Other',
      }
    ]

    this.deliverableTypes = []
    availableDeliverableTypes.forEach(item => {
      this.deliverableTypes.push({
        label: item.label,
        id: item.id,
        model: (this.block.availableDeliverableTypes || []).includes(item.id)
      })
    })
  }

  onMultiSelectChange(check, options) {
    this.block.availableDeliverableTypes = (options || []).filter(type => !!type.model).map(type => type.id);
  }

}
