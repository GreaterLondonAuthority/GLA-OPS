/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */


ErrorService.$inject = ['ConfirmationDialog'];

function ErrorService(ConfirmationDialog) {
  return {
    apiValidationHandler(callback) {
      return function errorHandler(err) {
        //If its not 400 it already caught by interceptor
        if (err && err.status === 400) {
          let errInfo = err.data || err.error || {};
          let modal = ConfirmationDialog.warn(errInfo.description || 'Failed validation on backend');
          if(callback){
            callback(err, modal);
          }
        }
      }.bind(this);
    }
  }
}

angular.module('GLA')
  .service('ErrorService', ErrorService);
