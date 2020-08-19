/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

describe('Service: ProjectDetailsService', () => {

  beforeEach(angular.mock.module('GLA'));

  let ProjectDetailsService, templateDetailsConfig, fields;

  beforeEach(inject($injector => {
    ProjectDetailsService = $injector.get('ProjectDetailsService');
    templateDetailsConfig = {
      addressRequirement: 'mandatory',
      boroughRequirement: 'mandatory',
      contactRequirement: 'optional',
      coordsRequirement: 'mandatory',
      descriptionRequirement: 'optional',
      imageRequirement: 'optional',
      interestRequirement: 'hidden',
      legacyProjectCodeRequirement: 'hidden',
      maincontactRequirement: 'mandatory',
      maincontactemailRequirement: 'mandatory',
      postcodeRequirement: 'optional',
      projectManagerRequirement: 'hidden',
      siteOwnerRequirement: 'hidden',
      siteStatusRequirement: 'hidden'
    }
    fields = ProjectDetailsService.fields(templateDetailsConfig);
  }));

  describe('fields', () => {
    it('title should be required by default', () => {
      expect(fields.title.required).toEqual(true);
      expect(fields.title.hidden).toBeFalsy();
      expect(fields.title.label).toEqual('Project title');
      expect(fields.title.placeholder).toEqual('e.g. name of site');
    });


    it('mainContanct should be required because of config', () => {
      expect(fields.mainContact.required).toEqual(true);
      expect(fields.mainContact.hidden).toBeFalsy();
      expect(fields.mainContact.label).toEqual('Confirm the main contact');
      expect(fields.mainContact.label).toEqual('Confirm the main contact');
    });

    it('planningPermissionReference should be optional by default (GLA-10550)', () => {
      expect(fields.planningPermissionReference.required).toBeFalsy();
      expect(fields.planningPermissionReference.hidden).toBeFalsy();
      expect(fields.planningPermissionReference.label).toEqual('Planning Permission Reference (optional)');
      expect(fields.planningPermissionReference.placeholder).toEqual('Provide the Planning Permission Reference number (optional)');
    });

    it('planningPermissionReference should be configurable (GLA-10614)', () => {
      templateDetailsConfig.planningPermissionReferenceRequirement = 'mandatory';
      fields = ProjectDetailsService.fields(templateDetailsConfig);
      expect(fields.planningPermissionReference.required).toBeTruthy();
      expect(fields.planningPermissionReference.hidden).toBeFalsy();
      expect(fields.planningPermissionReference.label).toEqual('Planning Permission Reference');
      expect(fields.planningPermissionReference.placeholder).toEqual('Provide the Planning Permission Reference number');
    });


    it('projectManager should be hidden', () => {
      expect(fields.projectManager.hidden).toBe(true);
    });


    it('fullAddress should be required (depends on address, borough, postcode)', () => {
      expect(fields.fullAddress.required).toEqual(true);
      expect(fields.fullAddress.label).toEqual('Enter the address of the project');
    });

    it('fullAddress should be required (depends on address, borough, postcode)', () => {
      delete templateDetailsConfig.addressRequirement;
      delete templateDetailsConfig.boroughRequirement;
      fields = ProjectDetailsService.fields(templateDetailsConfig);
      expect(fields.fullAddress.required).toEqual(false);
      expect(fields.fullAddress.label).toEqual('Enter the address of the project (optional)');
    });
  });
});
