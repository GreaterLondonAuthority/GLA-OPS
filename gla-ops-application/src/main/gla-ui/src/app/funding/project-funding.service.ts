import { Injectable } from '@angular/core';
import {isNumber} from "lodash-es";

@Injectable({
  providedIn: 'root'
})
export class ProjectFundingService {

  constructor() { }

  isActivityValid(activity: any) {
    activity.emptyNameWarning = !activity.name;

    if(
      !(
        isNumber(activity.capitalValue) ||
        isNumber(activity.capitalMatchFundValue) ||
        isNumber(activity.revenueValue) ||
        isNumber(activity.revenueMatchFundValue)
      )
    ) {
      activity.emptyBudgetsWarning = true;
    } else {
      activity.emptyBudgetsWarning = false;
    }
  }
}
