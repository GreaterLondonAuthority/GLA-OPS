import {Injectable} from '@angular/core';
import {isString, sortBy, startCase} from "lodash-es";

enum MilestoneType {
  NonMonetary,
  MonetarySplit,
  MonetaryValue
}

@Injectable()
export class TemplateBlockMilestonesService {

  constructor() {
  }

  sortProcessingRoutesAndMilestones(block) {
    block.processingRoutes = sortBy(block.processingRoutes, 'displayOrder');
    block.processingRoutes.forEach(pr => {
      pr.milestones = sortBy(pr.milestones, 'displayOrder');
    });
  }

  getApplicabilityOptions(): { id: string, label: string }[] {
    return [
      {
        id: 'NOT_APPLICABLE',
        label: 'N/A',
      }, {
        id: 'NEW_MILESTONES_ONLY',
        label: 'New milestones only',
      }, {
        id: 'ALL_MILESTONES',
        label: 'All milestones',
      }
    ]
  }

  getMilestoneTypeOptions(): { id: string; label: string }[] {
    let types = Object.values(MilestoneType).filter(mt => isString(mt));
    return (<string[]> types).map(mt => {
      return {
        id: mt,
        label: startCase(mt)
      }
    })
  }

  isMonetary(monetaryType: string): boolean {
    return MilestoneType[monetaryType] === MilestoneType.MonetaryValue || MilestoneType[monetaryType] === MilestoneType.MonetarySplit
  }

  isMonetarySplit(monetaryType: string): boolean {
    return MilestoneType[monetaryType] === MilestoneType.MonetarySplit
  }
}
