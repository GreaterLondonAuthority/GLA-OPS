/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

'use strict';
angular.module('GLA').directive('topMenu', function () {
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
    controller: function ($scope, UserService, FeatureToggleService) {
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
    }
  };
});


function isMobile(){
  return $('.navbar-toggle').is(':visible');
}
