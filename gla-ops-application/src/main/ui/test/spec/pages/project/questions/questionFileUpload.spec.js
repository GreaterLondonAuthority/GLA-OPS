/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

const utils = require('../../../../utils');

describe('Page: questions: QuestionFileUpload', () => {
  let config = {
    // getContracts: '.org-contract',
    // contractsCheckBoxes: '.org-contract .checkbox',
    // signedContract: '.org-contract .checkbox.signed.checked',
    // notRequiredContract: '.org-contract .checkbox.not-required.checked'
  };

  beforeEach(angular.mock.module('GLA'));

  let $compile, $rootScope, $scope, element, UserService, OrganisationService, ReferenceDataService, ctrl, $componentController, FileDeleteConfirmationModal;

  beforeEach(inject(function (_$componentController_) {
    $componentController = _$componentController_;
  }));


  beforeEach(inject($injector => {
    $compile = $injector.get('$compile');
    OrganisationService = $injector.get('OrganisationService');
    UserService = $injector.get('UserService');
    ReferenceDataService = $injector.get('ReferenceDataService');
    FileDeleteConfirmationModal = $injector.get('FileDeleteConfirmationModal');
    $rootScope = $injector.get('$rootScope');
    $scope = $rootScope.$new();

    // spyOn(ReferenceDataService, 'getBoroughs').and.returnValue(utils.mockPromise({}));

    $scope.question = {
      answerOptions: [],
      answerType: 'FileUpload',
      externalKey: '',
      id: 144,
      quantity: 5,
      text: 'Please upload supporting evidence',
      fileAttachments: [],
      attachments: []
    };
    $scope.readOnly = false;
    $scope.project = {
      organisation: {
        id: 123
      }
    };
    element = $compile('<question-file-upload question="question" read-only="readOnly" project="project"></question-file-upload>')($scope);
  }));

  describe('GLA-9758 - Read only view when no documents have been added', () => {
    it('Read only with no previous attachment', ()=> {
      $scope.readOnly = true;
      $scope.$digest();
      expect(element.text()).not.toContain('ADD DOCUMENT +');
      expect(element.text()).toContain('DOCUMENT NAME')
      expect(element.text()).toContain('UPLOAD DATE')
      expect(element.text()).toContain('UPLOADED BY')
      expect(element.text()).toContain('No files added.')
    });
    it('GLA-9762 - The delete, Save and Add Document CTAs do not display in Read Only view', ()=> {
      $scope.readOnly = true;
      $scope.question.fileAttachments = [{
        contentType: 'image/png',
        creatorName: 'test.admin@gla.com',
        createdOn: '2017-08-09T12:33:28.157+01:00',
        fileName: 'Screen Shot 2017-04-21 at 12.16.50.png',
        id: 10001,
        organisationId: 9999
      }];
      $scope.$digest();
      expect(element.text()).not.toContain('No files added.')
      expect(element.text()).not.toContain('DELETE')
      expect(element.text()).toContain('Screen Shot 2017-04-21 at 12.16.50.png   Aug 9, 2017 test.admin@gla.com')
    });

  });
  describe('GLA-9742 - Upload file is available as an answer type within the questions block', () => {
    it('show the add document button when in edit mode', () => {
      $scope.readOnly = false;
      $scope.$digest();
      expect(element.text()).toContain('Attachments');
      expect(element.text()).toContain('Max number of files: 5');
      expect(element.text()).toContain('Max file size per upload: 5MB');
      expect(element.text()).toContain('ADD DOCUMENT +');
    });
    it('it displays staged files', () => {
      $scope.readOnly = false;
      $scope.question.attachments = [{
        fileId: 123,
        fileName: 'Screen Shot 2017-01-23 at 16.38.49.png'
      }];
      $scope.$digest();
      expect(element.text()).toContain('Attachments');
      expect(element.text()).toContain('Max number of files: 5');
      expect(element.text()).toContain('Max file size per upload: 5MB');
      expect(element.text()).toContain('ADD DOCUMENT +');
      expect(element.find('.attached-files .staged-file').text()).toContain('Screen Shot 2017-01-23 at 16.38.49.png');
      expect(element.find('.attached-files .staged-file').text()).toContain('DELETE');
    });
  });
  describe('GLA-10247 - Configure number of documents in the template', () => {
    it('should block adding documents based on question config', () => {
      $scope.question.fileAttachments = [{
        contentType: 'image/png',
        createdBy: 'test.admin@gla.com',
        createdOn: '2017-08-09T12:33:28.157+01:00',
        fileName: 'document 1',
        id: 10001,
        organisationId: 9999
      },{
        contentType: 'image/png',
        createdBy: 'test.admin@gla.com',
        createdOn: '2017-08-09T12:33:28.157+01:00',
        fileName: 'document 2',
        id: 10002,
        organisationId: 9999
      }];
      $scope.$digest();

      expect(element.text()).toContain('ADD DOCUMENT +');
      expect(element.find('.fileUpload button[disabled]').length).toEqual(0);

      $scope.question.quantity = 2;

      $scope.$digest();

      expect(element.text()).toContain('ADD DOCUMENT +');
      expect(element.find('.fileUpload button[disabled]').length).toEqual(1);
    });
    it('should block adding documents based on staged and binded count total', () => {
      $scope.question.fileAttachments = [{
        contentType: 'image/png',
        createdBy: 'test.admin@gla.com',
        createdOn: '2017-08-09T12:33:28.157+01:00',
        fileName: 'document 1',
        id: 10001,
        organisationId: 9999
      }];
      $scope.question.attachments = [{
        fileId: 123,
        fileName: 'Screen Shot 2017-01-23 at 16.38.49.png'
      }];
      $scope.question.quantity = 2;
      $scope.$digest();

      expect(element.text()).toContain('ADD DOCUMENT +');
      expect(element.find('.fileUpload button[disabled]').length).toEqual(1);
    });
  });
  describe('quesiton file upload controller function tests', () => {
    it('onFileUploadProgress', () => {
        $scope.$digest();
        let $ctrl = element.isolateScope().$ctrl;
        $ctrl.onFileUploadProgress({progress: 10});
        expect($rootScope.showGlobalLoadingMask).toEqual(true)

    });
    it('onFileUploadComplete', () => {
        $scope.$digest();
        let $ctrl = element.isolateScope().$ctrl;
        $ctrl.onFileUploadComplete({
          response: {
            fileId: 123,
            fileName: 'Screen Shot 2017-01-23 at 16.38.49.png'
          }
        });
        expect($rootScope.showGlobalLoadingMask).toEqual(false);
        $scope.$digest();
        expect($ctrl.question.attachments.length).toEqual(1);

    });
    it('removeFileToUpload', () => {
        let file =  {
          fileId: 123,
          fileName: 'Screen Shot 2017-01-23 at 16.38.49.png'
        };
        $scope.question.attachments = [file];
        $scope.$digest();
        let $ctrl = element.isolateScope().$ctrl;
        expect($ctrl.question.attachments.length).toEqual(1);

        spyOn(FileDeleteConfirmationModal, 'show').and.returnValue({
          result: utils.mockPromise()
        });
        $ctrl.removeAttachment(file, $ctrl.question.attachments);
        expect($ctrl.question.attachments.length).toEqual(0);

    });
    it('removeAttachment', () => {
        let file =  {
          contentType: 'image/png',
          createdBy: 'test.admin@gla.com',
          createdOn: '2017-08-09T12:33:28.157+01:00',
          fileName: 'document 1',
          id: 10001,
          organisationId: 9999
        };
        $scope.question.fileAttachments = [file];
        $scope.$digest();
        let $ctrl = element.isolateScope().$ctrl;
        expect($ctrl.question.fileAttachments.length).toEqual(1);


        spyOn(FileDeleteConfirmationModal, 'show').and.returnValue({
          result: utils.mockPromise()
        });
        $ctrl.removeAttachment(file, $ctrl.question.fileAttachments);
        expect($ctrl.question.fileAttachments.length).toEqual(0);

    });
  });
});
