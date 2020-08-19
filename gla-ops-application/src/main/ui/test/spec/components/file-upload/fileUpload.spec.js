/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */


describe('Component: file-upload', () => {

  beforeEach(angular.mock.module('GLA'));

  let $compile, $rootScope, $scope, element, $timeout;


  beforeEach(inject($injector => {
    $compile = $injector.get('$compile');
    $timeout = $injector.get('$timeout');
    $rootScope = $injector.get('$rootScope');
    $scope = $rootScope.$new();

    $scope.$ctrl = {
      uploadParams: [],
      onProgress: ()=>{},
      onComplete: ()=>{},
      onError: ()=>{},
      readOnly: false
    };

    let template = `<file-upload class="add-document"
                      label="ADD DOCUMENT +"
                      upload-params="$ctrl.uploadParams"
                      on-progress="$ctrl.onProgress(event)"
                      on-complete="$ctrl.onComplete(event)"
                      on-error="$ctrl.onError(event)"
                      is-disabled="$ctrl.readOnly">
                    </file-upload>`;
    element = $compile(template)($scope);
    $timeout.flush();
  }));

  describe('#getFileInput()', () => {
    it('should be in sync with html template value (GLA-11501)', () => {
      let $ctrl = element.controller('fileUpload');
      expect($ctrl.getFileInput().length).toEqual(1);
    });
  });
});
