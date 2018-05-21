/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

/**
* Util Class for string manipulations
*/
'use strict';
ToastrUtil.$inject = ['$q', 'toastr', '$interval'];
function ToastrUtil($q, toastr, $interval) {
  let shownToasts = [];
  return {
    getShownToasts: function () {
      return shownToasts;
    },
    getToastKey: function(message, title, type) {
      return message+(title||'')+(type||'');
    },
    logToast: function(message, title, type) {
      shownToasts.push(this.getToastKey(message, title, type));
      console.log(shownToasts);
    },

    hasToastShown: function(message, title, type) {
      let self = this;
      var deferred = $q.defer();

      let intervalPromise = $interval(function (count) {
        console.log('search', self.getToastKey(message, title, type), shownToasts);
        if (shownToasts.includes(self.getToastKey(message, title, type))) {
          $interval.cancel(intervalPromise);
          deferred.resolve(true);
        }

        if(count === 30){
          console.log('error');
          $interval.cancel(intervalPromise);
          deferred.reject('Couldn\'t find "'+self.getToastKey(message, title, type)+'" in toasts in less than 30 sec');
        }

      }, 1000, 30);

      return deferred.promise;
    },
    hasSuccessToastShown: function(message, title) {
      return this.hasToastShown(message, title, 'success');
    },
    hasErrorToastShown: function(message, title) {
      return this.hasToastShown(message, title, 'error');
    },
    hasWarningToastShown: function(message, title) {
      return this.hasToastShown(message, title, 'warning');
    },
    hasInfoToastShown: function(message, title) {
      return this.hasToastShown(message, title, 'info');
    },

    success: function(message, title, override) {
      //  override.onShown = () => {
      //    console.log('onShown');
      //  };
      //  override.onShown.onHidden = () => {
      //    console.log('onHidden');
      //  };
      toastr.success(message, title, override);
      this.logToast(message, title, 'success');
    },
    error: function(message, title, override) {

      toastr.error(message, title, override);
      this.logToast(message, title, 'error');
    },
    info: function(message, title, override) {

      toastr.info(message, title, override);
      this.logToast(message, title, 'info');
    },
    warning: function(message, title, override) {
      toastr.warning(message, title, override);
      this.logToast(message, title, 'success');
    },
    clear: function() {
      toastr.clear();
    }
  };
}
angular.module('GLA')
.service('ToastrUtil', ToastrUtil);
