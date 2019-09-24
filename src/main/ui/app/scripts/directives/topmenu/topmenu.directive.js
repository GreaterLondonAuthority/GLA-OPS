/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

'use strict';
angular.module('GLA').directive('topMenu', function ($state) {
  return {
    restrict: 'E',
    templateUrl: 'scripts/directives/topmenu/topmenu.tpl.html',

    link: function($scope, $element, $attrs) {
      //Show on hover for desktop but click on mobile
      $('body').on('mouseover', '.navbar .dropdown', null, function(){
        if (!isMobile()) {
          $(this).addClass('open')
        }
      });

      $('body').on('mouseleave', '.navbar .dropdown', null, function(){
        if (!isMobile()) {
          $(this).removeClass('open')
        }
      });



      $('#top-menu-container').affix({ offset: { top: 125 } });

      var resizeNavBar = function() {
        // Resize element to the height of its children to avoid flickering
        $element.height($('#top-menu-container').height());

        if ($('#top-menu-container').hasClass('affix')) {
          var gap = ($(window).width() - $('header').width()) * 0.5;
          $('#top-menu-container').css('left', gap);
          $('#top-menu-container').css('right', gap);
        }
      };

      $('#top-menu-container').on('affixed.bs.affix', function() {
        resizeNavBar();
      });

      $(window).resize(function(){
        resizeNavBar();
      });

      // Watch height changes on container to avoid flickering
      $scope.$watch(function(){return $('#top-menu-container').height(); }, function(newValue, oldValue) {
        if (newValue != oldValue) {
          $element.height(newValue);
        }
      });
    },
    controllerAs: '$ctrl',
    controller: function ($scope, UserService, FeatureToggleService, MetadataService, ConfigurationService, SessionService) {

      this.shouldShowBanner = () => {
        let state = SessionService.getBannerMessageState();
        let show = false;
        if(this.systemOutageMessage && this.systemOutageMessage.text){
          // if same message as before

          if(state && state.message.text == this.systemOutageMessage.text){
            // if already dismissed
            show = !state.isDimissed;
          } else {
            show = true;
          }
        }
        return show;
      }

      ConfigurationService.systemOutageMessage().then((response) => {

        this.systemOutageMessage = {text: response.data};
      });

      MetadataService.subscribe((data)=>{

        $scope.$evalAsync(()=>{
          $scope.numberOfUnreadNotifications = data.numberOfUnreadNotifications;
          if(!data.loggedOut){
            this.systemOutageMessage = {text: data.systemOutageMessage};
            this.canDismissBanner = true;
          } else {
            this.canDismissBanner = false;
          }
        });
      });

      $scope.user = UserService.currentUser();
      FeatureToggleService.isFeatureEnabled('Notifications').then(resp => {
        $scope.notificationsEnabled = resp.data;
      });

      var navMain = $('body'); // avoid dependency on #id
      // "a:not([data-toggle])" - to avoid issues caused
      // when you have dropdown inside navbar
      // navMain.on('click', '.navbar-collapse a:not([data-toggle])', null, function () {
      $('body').on('click', '.navbar-collapse a:not([data-toggle])', null, function (e) {
        if (isMobile()) {
          let navMenu =  $('.navbar-collapse');
          navMenu.addClass('no-transition');
          navMenu.collapse('hide');
          setTimeout(()=>{
            navMenu.removeClass('no-transition');
          }, 0);
        }
      });
      $scope.$on('user.login', function () {
        $scope.user = UserService.currentUser();
      });

      $scope.$on('user.logout', function () {
        $scope.user = UserService.currentUser();
      });

      /**
       * Scrolls the window to the top of the page
       */
      $scope.scrollToTop = function() {
        $('html, body').animate({ scrollTop: 0 }, 'fast');
        setTimeout(()=>{
          $('top-menu').css('height', '');
        }, 0);
      };

      let menu = [
        menuItem('HOME', 'user', [] ),
        {
          title: 'ORGANISATIONS',
          items: [
            menuItem('All users', 'users', ['user.list.view.*'] ),
            menuItem('Manage organisations', 'organisations', [] ),
            menuItem('Consortiums & Partnerships', 'consortiums', ['cons'] ),
            menuItem('Teams', 'teams', ['team.view'] ),
          ]
        },
        {
          title: 'PROGRAMMES & PROJECTS',
          items: [
            menuItem('Programmes', 'programmes', ['prog'] ),
            menuItem('Projects', 'projects', ['proj'] ),
            menuItem('Assessments', 'assessments', ['assessment.view'] ),
          ]
        },
        {
          title: 'PAYMENTS',
          items: [
            menuItem('All Payments', 'all-payments', ['payments'] ),
            menuItem('Pending Payments', 'pending-payments', ['payments'] ),
          ]
        },
        menuItem('REPORTS', 'reports', ['reports.tab'] ),
        {
          title: 'SETTINGS',
          items: [
            menuItem('Project Templates', 'system-templates', ['system.dashboard'] ),
            menuItem('Questions', 'system-templates-questions', ['system.dashboard'] ),
            menuItem('Assessment Templates', 'assessment-templates', ['assessment.template.manage'] ),
            menuItem('Finance Categories', 'finance-categories', ['system.dashboard'] ),
            menuItem('Skills Profiles', 'skill-profiles', ['admin.skill.profiles'] ),
            menuItem('Outputs Configuration', 'outputs-configuration', ['outputsConfiguration.manage'] ),
            menuItem('Version Labels', 'preSetLabels', ['labels.manage'] ),
            menuItem('Notification Types', 'allNotifications', ['notification.list.view'] ),
            menuItem('Permissions', 'permissions', ['permission.list.view'] ),
          ]
        },
        {
          title: 'ADMIN',
          items: [
            menuItem('System Console', 'system', ['system.dashboard'] ),
            menuItem('Feature Toggles', 'system-features', ['system.dashboard'] ),
            menuItem('Messages', 'system-messages', ['system.dashboard'] ),
            menuItem('Audit History', 'audit-activity', ['system.dashboard'] ),
            menuItem('SAP Data Errors', 'system-sapData', ['system.dashboard'] ),
            menuItem('SQL Execution', 'sql', ['system.dashboard'] ),
            menuItem('Overrides', 'overrides', ['overrides.manage'] ),
          ]
        }
      ];

      $scope.menu = menu.map(topMenuItem => {
        let subMenuItems = topMenuItem.items || [];
        let combinedPermissions = [];
        let combinedStates = [];
        subMenuItems.forEach( item => {
          combinedPermissions = combinedPermissions.concat(item.permissions || []);
          combinedStates = combinedStates.concat(item.state || []);
        });
        topMenuItem.permissions = _.uniq((topMenuItem.permissions || []).concat(combinedPermissions));
        topMenuItem.states = _.uniq(combinedStates);
        return topMenuItem;
      });

      $scope.isMenuItemActive = function(menuItem){
        let activeStates = menuItem.state? [menuItem.state] : menuItem.states;
        return _.some(activeStates, s => $state.includes(s));
      };

      $scope.goToState = function(state, e){
        if(state) {
          $state.go(state);
        }
      };
    }
  };
});

function menuItem(title, state, permissions) {
    return {
      title: title,
      state: state,
      permissions: permissions
    }
}

function isMobile(){
  return $('.navbar-toggle').is(':visible');
}
