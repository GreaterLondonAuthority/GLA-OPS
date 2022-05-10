import {Component, EventEmitter, Input, OnInit, Output, ViewEncapsulation} from '@angular/core';
import {SessionService} from "../session/session.service";
import {some, uniq} from "lodash-es";
import {NavigationService} from "../navigation/navigation.service";
import { FeatureToggleService } from '../feature-toggle/feature-toggle.service';

declare var $: any;

@Component({
  selector: 'gla-top-menu',
  templateUrl: './top-menu.component.html',
  styleUrls: ['./top-menu.component.scss'],
  encapsulation: ViewEncapsulation.None
})
export class TopMenuComponent implements OnInit {

  @Output() mobileMenuExpandedChange = new EventEmitter<boolean>()
  @Input() mobileMenuExpanded = false;
  programmeAllocationsPageFeatureEnabled : boolean = false;
  menu: void;

  constructor(private sessionService: SessionService,
              private featureToggleService: FeatureToggleService,
              private navigationService: NavigationService) { }

  ngOnInit(): void {
    this.featureToggleService.isFeatureEnabled('ProgrammeAllocationsPage').subscribe(enabled => {
      this.programmeAllocationsPageFeatureEnabled = enabled
      this.menu = this.getMenu();
    })
  }

  private getMenu() {
    let menu: any = [
      this.menuItem('HOME', 'user', [], null),
      {
        title: 'ORGANISATIONS',
        items: [
          this.menuItem('All users', 'users', ['user.list.view.*'], null),
          this.menuItem('Manage organisations', 'organisations', [], null),
          this.menuItem('Consortiums & Partnerships', 'consortiums', ['cons'], null),
          this.menuItem('Teams', 'teams', ['team.view'], null),
        ]
      },
      {
        title: 'PROGRAMMES & PROJECTS',
        items: [
          this.menuItem('Assessments', 'assessments', ['assessment.view'], null),
          this.menuItem('Programmes', 'programmes', ['prog'], null),
          this.menuItem('Programme Allocations', 'programme-allocations', ['view.programme.allocations'], null, this.programmeAllocationsPageFeatureEnabled),
          this.menuItem('Projects', 'projects', ['proj'], null),
        ]
      },
      {
        title: 'PAYMENTS',
        items: [
          this.menuItem('All Payments', 'all-payments', ['payments'], null),
          this.menuItem('Pending Payments', 'pending-payments', ['payments'], null),
        ]
      },
      this.menuItem('REPORTS', 'reports', ['reports.tab'], null),
      {
        title: 'SETTINGS',
        items: [
          this.menuItem('Assessment Templates', 'assessment-templates', ['assessment.template.manage'], null),
          this.menuItem('Categories', 'categories', ['system.dashboard'], null),
          this.menuItem('Contract Types', 'contract-types', ['admin.contract.types'], null),
          this.menuItem('Notification Types', 'allNotifications', ['notification.list.view'], null),
          this.menuItem('Permissions', 'permissions', ['permission.list.view'], null),
          this.menuItem('Project Templates', 'system-templates', ['temp.manage'], null),
          this.menuItem('Questions', 'system-templates-questions', ['temp.manage'], null),
          this.menuItem('Skills Profiles', 'skill-profiles', ['admin.skill.profiles'], null),
          this.menuItem('Version Labels', 'preSetLabels', ['labels.manage'], null),
        ]
      },
      {
        title: 'ADMIN',
        items: [
          this.menuItem('Audit History', 'audit-activity', ['system.dashboard'], null),
          this.menuItem('Block Usage', 'block-usage', ['block.usage'], null),
          this.menuItem('Broadcasts', 'broadcasts', ['broadcast'], null),
          this.menuItem('Email Status Reports', 'email-reports', ['email.reports'], null),
          this.menuItem('Feature Toggles', 'system-features', ['system.dashboard'], null),
          this.menuItem('Messages', 'system-messages', ['system.dashboard'], null),
          this.menuItem('Overrides', 'overrides', ['overrides.manage'], null),
          this.menuItem('SAP Data Errors', 'system-sapData', ['sap.errors'], null),
          this.menuItem('Scheduled Notifications', 'scheduledNotifications', ['notification.schedule'], null),
          this.menuItem('SQL Execution', 'sql', ['system.dashboard'], null),
          this.menuItem('System Console', 'system', ['system.dashboard'], null),
        ]
      }
    ];

    return menu.map(topMenuItem => {
      let subMenuItems = topMenuItem.items || [];
      let combinedPermissions = [];
      let combinedStates = [];
      subMenuItems.forEach( item => {
        combinedPermissions = combinedPermissions.concat(item.permissions || []);
        combinedStates = combinedStates.concat(item.state || []);
      });
      topMenuItem.permissions = uniq((topMenuItem.permissions || []).concat(combinedPermissions));
      topMenuItem.states = uniq(combinedStates);
      return topMenuItem;
    });

  }

  goToState(state){
    if(state) {
      this.navigationService.goToUiRouterState(state, null, {reload: true});
      this.onPageChange();
    }
  };

  private menuItem(title, state, permissions, icon, featureEnabled = true) {
    return {
      title: title,
      state: state,
      permissions: permissions,
      icon: icon,
      featureEnabled: featureEnabled
    }
  }

  isMenuItemActive(menuItem){
    let activeStates = menuItem.state? [menuItem.state] : menuItem.states;
    //checks in ng1's ui-router until it is migrated
    return some(activeStates, s => this.navigationService.uiRouterStateIncludes(s));
  };

  onPageChange(){
    this.mobileMenuExpanded = false;
  }

  /**
   * Scrolls the window to the top of the page
   */
  scrollToTop(){
    $('html, body').animate({ scrollTop: 0 }, 'fast');
    this.onPageChange();
  };

  isActive(state: any) {

  }
}
