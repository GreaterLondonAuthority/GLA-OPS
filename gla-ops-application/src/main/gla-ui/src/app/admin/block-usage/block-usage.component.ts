import {Component, Input, OnInit} from '@angular/core';
import {ReferenceDataService} from "../../reference-data/reference-data.service";
import {NavigationService} from "../../navigation/navigation.service";
import {sortBy} from "lodash-es";

@Component({
  selector: 'gla-block-usage',
  templateUrl: './block-usage.component.html',
  styleUrls: ['./block-usage.component.scss']
})
export class BlockUsageComponent implements OnInit {
  blockUsages: any[] = [];
  @Input() blockTypes: any[];
  @Input() internalBlockTypes: any[];
  internalOrExternal: any;
  externalBlockType: any;
  internalBlockType: any;

  constructor(private referenceDataService: ReferenceDataService,
              private navigationService: NavigationService) { }

  ngOnInit(): void {
    this.blockTypes = sortBy(this.blockTypes, 'displayName');
    this.internalBlockTypes = sortBy(this.internalBlockTypes, 'displayName');
  }

  onInternalOrExternalBlockSelect(internalOrExternal) {
    this.clearSelections()
    this.internalOrExternal = internalOrExternal;
  }

  onBlockTypeSelect(blockType) {
    this.referenceDataService.getBlockUsage(this.externalBlockType, this.internalBlockType).subscribe(resp => {
        this.blockUsages = resp as any[];
      }
    );
  }

  goToTemplate(templateId: any) {
    this.navigationService.goToUiRouterState('system-template-details', {templateId: templateId});
  }

  goToProgramme(programmeId: any) {
    this.navigationService.goToUiRouterState('programme', {programmeId: programmeId});
  }

  clearSelections() {
    this.blockUsages = [];
    this.internalOrExternal = "";
    this.externalBlockType = "";
    this.internalBlockType = "";
  }

}
