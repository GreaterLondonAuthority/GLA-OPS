/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

Downgrade.$inject = ['$timeout', '$q'];

function Downgrade($timeout, $q) {
  return {
    /**
     * There are issues with protractor waiting indefinitely for downgraded ng9 observable.toPromise() and timing out.
     * @param observable
     * @returns {*}
     */
    toPromise(observable) {
      let deferred = $q.defer();
      observable.subscribe(
        data => $timeout(() => deferred.resolve(data)),
        err => $timeout(() => deferred.reject(err))
      );
      return deferred.promise;
    }
  };
}

angular.module('GLA')
  .service('Downgrade', Downgrade);
