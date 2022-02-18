import {Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges} from '@angular/core';
import {ReferenceDataService} from "../../reference-data/reference-data.service";
import {ProjectDetailsService} from "./project-details.service";
import {startCase} from "lodash-es";

@Component({
  selector: 'gla-template-block-details',
  templateUrl: './template-block-details.component.html',
  styleUrls: ['./template-block-details.component.scss']
})
export class TemplateBlockDetailsComponent implements OnInit, OnChanges {

  @Input() block: any
  @Input() template: any
  @Input() readOnly: boolean
  @Input() editable: boolean
  @Output() onChange: EventEmitter<any> = new EventEmitter()
  requirementOptions: any
  configurableFields: any


  constructor(private projectDetailsService: ProjectDetailsService,
              private referenceDataService: ReferenceDataService) {
  }

  ngOnInit(): void {
    const customFieldLabel = {
      maincontactRequirement: {
        label: 'Main Contact Requirement'
      },
      maincontactemailRequirement: {
        label: 'Main Contact Email Requirement'
      },
      wardIdRequirement: {
        label: 'Ward Requirement',
      },
      interestRequirement: {
        label: 'Ownership/Legal Interest Requirement',
      },
      developmentLiabilityOrganisationRequirement: {
        label: 'Development Liability Organisation Requirement (Consortium Only)',
      },
      postCompletionLiabilityOrganisationRequirement: {
        label: 'Post Completion Development Liability Organisation'
          + ' Requirement (Consortium Only)',
      }
    }

    const detailsConfigDefaultValues = {
      secondaryContactRequirement: 'hidden',
      secondaryContactEmailRequirement: 'hidden',
    }

    if(!this.template.detailsConfig){
      this.template.detailsConfig = {};
    }

    Object.keys(detailsConfigDefaultValues).forEach(key => {
      if (this.template.detailsConfig[key] == null) {
        this.template.detailsConfig[key] = detailsConfigDefaultValues[key];
      }
    });

    const simpleProjectDetailsFields = ['maxBoroughs']
    this.configurableFields = this.projectDetailsService.getDetailsConfigurableFields().map(fieldId => {
      let fieldConfig = customFieldLabel[fieldId] || {};
      return {
        id: fieldId,
        label: (fieldConfig.label) || startCase(fieldId),
        displayAsSelect: (simpleProjectDetailsFields.indexOf(fieldId.toString()) !== -1) ? false : true
      }
    });
    this.requirementOptions = this.referenceDataService.getRequirementOptions();
  }

  ngOnChanges(changes: SimpleChanges): void {
    console.log('ngOnChanges', this.template.detailsConfig)
  }

  trackByKey(index: number, obj: any): string {
    return obj.id;
  };

  canDisplayMaxBorough(field) {
    return field === 'maxBoroughs' && this.template.detailsConfig &&
      (this.template.detailsConfig['boroughRequirement'] === 'mandatory'
        || this.template.detailsConfig['boroughRequirement'] === 'optional'
      )
  }
}
