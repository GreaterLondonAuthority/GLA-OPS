/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

const utils = require('../../utils');

describe('Controller: ConsrotiumFormCtrl', () => {

  beforeEach(angular.mock.module('GLA'));

  let ctrl, $componentController, OrganisationGroupService;

  beforeEach(inject($injector => {
    $componentController = $injector.get('$componentController');
    OrganisationGroupService = $injector.get('OrganisationGroupService');
  }));


  describe('organisations already in projects', () => {
    beforeEach(() => $ctrl(bindings()));

    it('should be true if in the list of restricted', () => {
      expect(ctrl.isOrganisationInAnyProject(10)).toBe(true);
    });

    it('should be false if not in the list of restricted', () => {
      expect(ctrl.isOrganisationInAnyProject(1)).toBe(false);
    });
  });

  describe('organisations', () => {
    let consortium = {
      id: 1,
      leadOrganisationId: 1,
      organisations: [
        {id: 1, name: 'LeadOrg1'},
        {id: 2, name: 'Some organisation'}
      ]
    };

    beforeEach(() => {
      $ctrl(bindings({data: consortium}))
    });

    it('should not have the lead organisation in the list of organisations on load', () => {
      expect(ctrl.data.organisations.length).toBe(1);
      expect(ctrl.data.organisations[0].id).toBe(2);
    });

    it('should have the lead organisation in the list on save', () => {
      spyOn(ctrl, 'onSave').and.callFake(param => {
        let data = param.$event;
        expect(data.id).toBe(1);
        expect(data.organisations.length).toBe(2);
        expect(_.find(data.organisations, {id: 1})).toBeTruthy();
        expect(_.find(data.organisations, {id: 2})).toBeTruthy();
      });

      ctrl.save();
      expect(ctrl.onSave).toHaveBeenCalled();
    });

    it('should lookup organisation with the api', () => {
      let fakeOrg = {
        code: '10000',
        name: 'Some Org'
      };
      spyOn(OrganisationGroupService, 'lookupOrgNameByCodeForConsortium').and.returnValue(utils.mockPromise({status: 200, data: fakeOrg}));
      $ctrl(bindings({data: consortium}));
      ctrl.orgCode = fakeOrg.code;
      ctrl.lookupOrganisationCode(fakeOrg.code);
      expect(OrganisationGroupService.lookupOrgNameByCodeForConsortium).toHaveBeenCalledWith(fakeOrg.code);
      expect(ctrl.org).toEqual(fakeOrg);
    });
  });


  function $ctrl(bindings) {
    ctrl = $componentController('consortiumForm', {OrganisationGroupService}, bindings);
    ctrl.$onInit();
    return ctrl;
  }

  function bindings(bindings) {
    let defaultBinding = {
      programmes: [{id: 1, name: 'Programme1'}, {id: 1, name: 'Programme2'}],
      data: {},
      organisationsInProjects: [{id: 10, name: 'OrgInProject1'}, {id: 11, name: 'OrgInProject2'}],
      leadOrganisations: [{id: 1, name: 'LeadOrg1'}],
      onSave: angular.noop
    };
    return _.assign(defaultBinding, bindings);
  }
});
