/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

'use strict';
/**
 * Attribute Directive to help get round the two click issue.
 * If a field in the page has a ng-blur event and that field currently has the
 * focus, then the ng-click event to any button will be consumbed by the blur.
 *
 * this directve is be be attached on the buttons who could be affected such as
 * save.
 * a state variable will be set to true at the begining of the click event
 * and will set back to false at the end.
 *
 * then in the controller of the blur event, we need to check the state of that
 * variable and change the behavior from blur callback to the click action callback.
 *
 * this is not perfect but there is no way to prevent the blur from consuming the
 * click event.
 *
 * please note: we have to use the double dot for the state
 * $scope.mouseDownState.state = true;
 *
 * this is to ensure that the parent controller can see the change without
 * depending of the binding.
 * $scope.mouseDownState = true; would update the parent controller value after
 * the click finished (following digest)
 */
angular.module('GLA')
  .directive('mouseEventForAutosave', function() {
    return {
      restrict: 'A',
      scope: {
        mouseDownState: '='
      },
      link: function($scope, element) {
        // don't bind if there is no control variable
        // not all back buttons need this but pageHeader will bind this directive
        // all the time
        if($scope.mouseDownState){
          element.bind('mousedown', function(){
            $scope.mouseDownState.state = true;
          });
          element.bind('mouseup', function(){
            $scope.mouseDownState.state = false;
          });
          element.bind('mouseleave', function(){
            $scope.mouseDownState.state = false;
          });
        }
      }
    };
  });
