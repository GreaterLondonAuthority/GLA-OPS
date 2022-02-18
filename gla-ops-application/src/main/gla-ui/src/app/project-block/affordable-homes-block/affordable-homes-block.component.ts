import {Component, Injector, Input} from '@angular/core';
import {ProjectBlockComponent} from "../project-block.component";
import {find, sortBy} from "lodash-es";
import {Subscription} from "rxjs";

@Component({
  selector: 'gla-indicative-starts-and-completions-block',
  templateUrl: './affordable-homes-block.component.html',
  styleUrls: ['./affordable-homes-block.component.scss']
})
export class AffordableHomesBlockComponent extends ProjectBlockComponent {

  @Input() template: any
  templateBlock: any
  startOnSiteEntries: any[] = []
  completionEntries: any[] = []
  startOnSiteTotals: any
  completionTotals: any
  startOnSiteValidationError: any
  ofWhichTotalSosUnitsValidationError: any
  completionValidationError: any
  ofWhichTotalCompletionUnitsValidationError: any
  startOnSiteMatchingCompletionValidationError: any
  summaryTotals: any
  costs: any
  contributions: any
  totalCosts: any
  totalContributions: any
  grantRequestedTotals: any
  totalCostsPercentage: any
  startsOnSiteLocked: false
  completionsLocked: false
  tenureTypes: any[]

  private tableAutoSaveSubscription: Subscription

  constructor(injector: Injector) {
    super(injector);
  }

  ngOnInit(): void {
    super.ngOnInit();

    this.initDataFromProjectBlock();

    this.templateBlock = find(this.template.blocksEnabled, {block: 'AffordableHomes'});
    this.tenureTypes = sortBy(this.template.tenureTypes, 'displayOrder');
  }

  initDataFromProjectBlock() {
    this.startOnSiteEntries = [];
    this.completionEntries = [];
    this.projectBlock.entries.forEach(entry => {
      if (entry.type === 'StartOnSite') {
        this.startOnSiteEntries.push(entry);
      }
      if (entry.type === 'Completion') {
        this.completionEntries.push(entry);
      }
    });
    this.startOnSiteTotals = this.projectBlock.totals['StartOnSite'];
    this.completionTotals = this.projectBlock.totals['Completion'];
    this.summaryTotals = this.projectBlock.summaryTotals;
    this.costs = this.projectBlock.costs;
    this.contributions = this.projectBlock.contributions;
    this.totalContributions = this.summaryTotals.totalContributions;
    this.totalCosts = this.summaryTotals.totalCosts;
    this.grantRequestedTotals = this.projectBlock.grantRequestedTotals;
    this.totalCostsPercentage = this.projectBlock.totalCostsPercentage;
    this.startsOnSiteLocked = this.projectBlock.startOnSiteMilestoneAuthorised;
    this.completionsLocked = this.projectBlock.completionMilestoneAuthorised;

    this.initValidationErrors();
  }

  initValidationErrors() {
    this.startOnSiteValidationError = null;
    this.ofWhichTotalSosUnitsValidationError = null;
    this.completionValidationError = null;
    this.ofWhichTotalCompletionUnitsValidationError = null;
    this.startOnSiteMatchingCompletionValidationError = null;
    if (this.projectBlock.validationFailures) {
      if (this.projectBlock.validationFailures.startOnSiteUnits) {
        this.startOnSiteValidationError = this.projectBlock.validationFailures.startOnSiteUnits[0].description;
      }
      if (this.projectBlock.validationFailures.ofWhichTotalSosUnits) {
        this.ofWhichTotalSosUnitsValidationError = this.projectBlock.validationFailures.ofWhichTotalSosUnits[0].description;
      }
      if (this.projectBlock.validationFailures.completionUnits) {
        this.completionValidationError = this.projectBlock.validationFailures.completionUnits[0].description;
      }
      if (this.projectBlock.validationFailures.ofWhichTotalCompletionUnits) {
        this.ofWhichTotalCompletionUnitsValidationError = this.projectBlock.validationFailures.ofWhichTotalCompletionUnits[0].description;
      }
      if (this.projectBlock.validationFailures.totalUnits) {
        this.startOnSiteMatchingCompletionValidationError = this.projectBlock.validationFailures.totalUnits[0].description;
      }
    }
  }

  autoSave() {
    let saveObservable = this.withLock(this.save(false));
    //Cancel pending table triggered request to avoid backend response overriding values typed in by user
    if(this.tableAutoSaveSubscription){
      this.tableAutoSaveSubscription.unsubscribe();
    }

    this.tableAutoSaveSubscription = saveObservable.subscribe(rsp => {
      this.projectBlock = rsp;
      this.initDataFromProjectBlock();
    });
  }

  updateGrantRequestedValue() {
    this.projectBlock.grantRequestedEntries.forEach(entry => {
      if (this.isGrantEntry(entry)) {
        this.projectBlock.zeroGrantRequested ? entry.value = 0 : entry.value = undefined
      }
    })
  }

  updateCompletionOnly() {
    this.projectService.updateProjectAffordableHomesBlock(this.project.id, this.projectBlock, true).subscribe(rsp => {
      this.projectBlock = rsp;
    });
    this.autoSave();
  }

  isGrantEntry(entry){
    return (this.templateBlock.grantTypes.includes(entry.type)) ? true: false;
  }

  showNilGrantCheckbox() {
    return !this.templateBlock.nilGrantHidden && !this.grantValueEntered()
  }

  grantValueEntered(){
    let grantValueEntered = false
    this.projectBlock.grantRequestedEntries.forEach(entry => {
      if (this.isGrantEntry(entry) && entry.value > 0) {
        grantValueEntered = true;
      }
    })
    return grantValueEntered
  }

  submit() {
    return this.save(true);
  }

  save(releaseLock: Boolean) {
    return this.projectBlockService.updateBlock(this.project.id, this.projectBlock.id, this.projectBlock, releaseLock);
  }
}
