/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

const utils = require('../../../utils');
const responses = require('./responses');

describe('Page: change report', () => {
  let config = {
    // getContracts: '.org-contract',
    // contractsCheckBoxes: '.org-contract .checkbox',
    // signedContract: '.org-contract .checkbox.signed.checked',
    // notRequiredContract: '.org-contract .checkbox.not-required.checked'
  };

  beforeEach(angular.mock.module('GLA'));

  let $compile, $rootScope, $scope, $httpBackend, element, ProjectService, UserService, OrganisationService, OrganisationGroupService, ReferenceDataService, ctrl, $componentController, TemplateService;

  beforeEach(inject(function (_$componentController_) {
    $componentController = _$componentController_;
  }));


  beforeEach(inject($injector => {
    $compile = $injector.get('$compile');
    ProjectService = $injector.get('ProjectService');
    TemplateService = $injector.get('TemplateService');
    OrganisationService = $injector.get('OrganisationService');
    UserService = $injector.get('UserService');
    ReferenceDataService = $injector.get('ReferenceDataService');
    OrganisationGroupService = $injector.get('OrganisationGroupService');
    $rootScope = $injector.get('$rootScope');
    $scope = $rootScope.$new();

    $httpBackend = $injector.get('$httpBackend');



    $httpBackend.when('GET', '/api/v1/paymentSources').respond(JSON.parse(responses.paymentSources));
    $httpBackend.when('GET', '/api/v1/projects/123').respond(JSON.parse(responses.lastest));
    $httpBackend.expectGET('/api/v1/projects/123');

    $httpBackend.when('GET', '/api/v1/projects/123?unapprovedChanges=false').respond(JSON.parse(responses.lastApproved));
    $httpBackend.expectGET('/api/v1/projects/123?unapprovedChanges=false');

    $httpBackend.when('GET', '/api/v1/templates/234').respond(JSON.parse(responses.template));
    $httpBackend.expectGET('/api/v1/templates/234');




    ProjectService.getProject(123).then((data)=>{
      $scope.latestProject = data.data;
    });
    ProjectService.getProject(123, {unapprovedChanges: false}).then((data)=>{
      $scope.lastApprovedProject = data.data;
    });
    TemplateService.getTemplate(234).then((data)=>{
      $scope.template = data.data;
    });

    // $httpBackend.when('GET', window.location.origin+'/api/v1/projects/234/history').respond(JSON.parse(responses.history));
    // $httpBackend.expectGET(window.location.origin+'/api/v1/projects/234/history');
    //
    // ProjectService.getProjectHistory(234).then((data)=>{
    //   $scope.projectHistory = data;
    // });

    $httpBackend.flush();

    spyOn(UserService, 'currentUser').and.returnValue({permissions: []});
    spyOn(ReferenceDataService, 'getBoroughs').and.returnValue(utils.mockPromise({}));
    spyOn(OrganisationGroupService, 'findById').and.returnValue(utils.mockPromise({data:{organisations:[]}}));
    element = $compile('<change-report-page latest-project="latestProject" last-approved-project="lastApprovedProject" template="template" project-history="projectHistory"></change-report-page>')($scope);
  }));

  describe('Block display', () => {
    it('should display that number of block in that order', ()=> {
      // spyOn(ProjectService, 'getProjectHistory').and.returnValue(utils.mockPromise(JSON.parse(responses.history)));
      $scope.projectHistory = JSON.parse(responses.history);
      $scope.$digest();
      // element.find('.block-name').map((a,b)=>console.log(a,b.text));
      expect(element.find('.block-name').map((index, item)=> $(item).text()).get()).toEqual(['Project Details', 'Additional Questions', 'Milestones', 'Calculate Grant', 'Design Standards', 'Grant Source', 'Project History']);
    });

    it('should start of opened', ()=> {
      // spyOn(ProjectService, 'getProjectHistory').and.returnValue(utils.mockPromise(JSON.parse(responses.history)));
      $scope.projectHistory = JSON.parse(responses.history);
      $scope.$digest();
      let $ctrl = element.isolateScope().$ctrl;
      let allExpended = true;
      $ctrl.blocksToCompare.forEach((item) => {
        if(!item.expanded){
          allExpended = false;
        }
      });
      expect(allExpended).toBe(true);

    });
  });

  describe('Change report history', () => {
    it('don\'t show project history if not comments in history GLA-9364 - No comments', ()=> {
      // spyOn(ProjectService, 'getProjectHistory').and.returnValue(utils.mockPromise(JSON.parse(responses.historyNoComments)));
      $scope.projectHistory = JSON.parse(responses.historyNoComments);
      $scope.$digest();
      expect(element).not.toContain('Project History');
    });
    it('should show last project history item with comment GLA-9108 - Display latest project comment in the CME change report', ()=> {
      // spyOn(ProjectService, 'getProjectHistory').and.returnValue(utils.mockPromise(JSON.parse(responses.history)));
      $scope.projectHistory = JSON.parse(responses.history);
      $scope.$digest();
      let $ctrl = element.isolateScope().$ctrl;
      expect(element.find('.project-history-block').text()).toContain('first comment on second item');
    });
    it('GLA-9108 - Display date, name and last status as saved in Project history', ()=> {
      // spyOn(ProjectService, 'getProjectHistory').and.returnValue(utils.mockPromise(JSON.parse(responses.history)));
      $scope.projectHistory = JSON.parse(responses.history);
      $scope.$digest();
      let $ctrl = element.isolateScope().$ctrl;
      expect(element.find('.project-history-block .metainformation').text()).toContain('04/08/2017 at 10:19 Approved project amended by Test User');
      expect(element.find('.project-history-block .comment').text()).toContain('first comment on second item');
    });
    it('GLA-8946 - Do not display a jump CTA for the last section in the report', ()=> {
      // spyOn(ProjectService, 'getProjectHistory').and.returnValue(utils.mockPromise(JSON.parse(responses.history)));
      $scope.projectHistory = JSON.parse(responses.history);
      $scope.$digest();
      expect(element.find('.project-history-block').text()).not.toContain('Jump to');
    });


  });
});
