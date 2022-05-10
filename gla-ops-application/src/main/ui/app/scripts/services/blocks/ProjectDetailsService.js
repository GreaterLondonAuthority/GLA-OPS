/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

ProjectDetailsService.$inject = [];

function ProjectDetailsService() {

  return {
    /**
     * Returns project details fields definitions. Some of them are configurable inside template
     * @param templateDetailsConfig Details filed visibility from the template
     */
    fields(templateDetailsConfig){
      /**
       *
       * By default most of the fields are optional but we have some cases where they are mandatory even though its not specified in template config
       */
      let fields = {
        title: {
          label: 'Project title',
          placeholder: 'e.g. name of site',
          configFieldName: '',
          required: true,
        },

        organisationGroupId: {
          label: 'Bidding Arrangement',
          placeholder: 'Select a bidding arrangement',
          required: true
        },

        developingOrganisationId: {
          label: 'Developing organisation',
          placeholder: 'Developing organisation',
          required: true
        },

        developmentLiabilityOrganisationId: {
          label: 'Organisation with grant liability during development',
          placeholder: 'Organisation with grant liability during development',
          configFieldName: 'developmentLiabilityOrganisation'
        },

        postCompletionLiabilityOrganisationId: {
          label: 'Organisation with grant liability after completion',
          placeholder: 'Organisation with grant liability after completion',
          configFieldName: 'postCompletionLiabilityOrganisation'
        },

        description: {
          label: 'Enter a brief project description',
          placeholder: 'Provide an overview of project objectives and deliverables, max 1,000 characters'
        },

        sapId: {
          label: 'SAP ID for payments related to this project',
          placeholder: 'Select a SAP ID'
        },

        address: {
          label: 'Enter the address of the project',
          placeholder: 'Provide the address of the project'
        },

        borough: {
          label: 'Enter the borough the project lies in',
          placeholder: 'Provide the borough of the project'
        },

        postcode: {
          label: 'Enter the postcode of the project',
          placeholder: 'Provide the postcode of the project'
        },

        //This is an exception. Fake config for grouped address field
        fullAddress: {
          label: 'Enter the address of the project',

          get required() {
            return ['address', 'borough', 'postcode'].some(fieldName => fields[fieldName].required);
          },

          get hidden() {
            return fields.address.hidden;
          },

          skipTemplateConfig: true,
          group: true
        },

        address: {
          label: 'Enter the address of the project',
          placeholder: 'First Line'
        },

        borough: {
          label: 'Borough',
          placeholder: 'Select a borough'
        },

        //TODO: became optional as default
        postcode: {
          label: 'Postcode',
          placeholder: 'Postcode'
        },

        //This is an exception. Fake config for grouped address field
        coords: {
          label: 'Co-ordinates',
          group: true
        },

        coordX: {
          configFieldName: 'coords',
          placeholder: 'X',
          required: true,
        },

        coordY: {
          configFieldName: 'coords',
          placeholder: 'Y',
          required: true
        },

        //TODO: visibility on address
        wardId: {
          label: 'Ward',
          placeholder: 'Ward'
        },

        mainContact: {
          label: 'Confirm the main contact',
          placeholder: 'Confirm the main contact',
          configFieldName: 'maincontact'
        },

        mainContactEmail: {
          label: 'Confirm email address of the main contact',
          placeholder: 'Confirm email address of the main contact',
          configFieldName: 'maincontactemail'
        },


        secondaryContact: {
          label: 'Confirm the secondary contact',
          placeholder: 'Confirm the secondary contact',
          configFieldName: 'secondaryContact'
        },

        secondaryContactEmail: {
          label: 'Confirm email address of the secondary contact',
          placeholder: 'Confirm email address of the secondary contact',
          configFieldName: 'secondaryContactEmail'
        },

        siteOwner: {
          label: 'Enter name of site owner',
          placeholder: 'Site Owner'
        },

        interest: {
          label: 'Enter type of ownership or legal interest',
          placeholder: 'Legal interest'
        },

        projectManager: {
          label: 'Project Manager',
          placeholder: 'Project Manager'
        },

        siteStatus: {
          label: 'Site Status',
          placeholder: 'Select a site status'
        },

        legacyProjectCode: {
          label: 'Legacy project code',
          placeholder: 'Legacy project code'
        },

        planningPermissionReference: {
          label: 'Planning Permission Reference',
          placeholder: 'Provide the Planning Permission Reference number'
        }
      };

      //Update required/hidden config based on template config
      Object.keys(fields).forEach(fieldName => {
        fields[fieldName] = this.getFieldConfig(fieldName, fields, templateDetailsConfig);
      });

      //Suffix label and placeholder with '(optional)'
      Object.keys(fields).forEach(fieldName => {
        let fieldConfig = fields[fieldName];
        if (!fieldConfig.required) {
          let optionalFieldSuffix = ' (optional)';
          fieldConfig.label += optionalFieldSuffix;
          fieldConfig.placeholder += optionalFieldSuffix;
        }
      });

      return fields;
    },

    /**
     * Update required/hidden config based on template config
     * Merges default field config with the template configuration     *
     */
    getFieldConfig(fieldName, defaultFieldsConfig, templateDetailsConfig) {
      let fieldConfig = defaultFieldsConfig[fieldName] || {};
      if (templateDetailsConfig && !fieldConfig.skipTemplateConfig) {
        let configFieldName = `${fieldConfig.configFieldName || fieldName}Requirement`;
        let templateFieldConfig = templateDetailsConfig[configFieldName];

        if (templateFieldConfig === 'mandatory') {
          fieldConfig.required = true;
        }

        if (templateFieldConfig === 'optional') {
          fieldConfig.required = false;
        }

        if (templateFieldConfig === 'hidden') {
          fieldConfig.hidden = true;
        }
      }

      return fieldConfig;
    },

    getDetailsConfigurableFields() {
      return [
        'descriptionRequirement',
        'addressRequirement',
        'boroughRequirement',
        'maxBoroughs',
        'wardIdRequirement',
        'postcodeRequirement',
        'coordsRequirement',
        'maincontactRequirement',
        'maincontactemailRequirement',
        'secondaryContactRequirement',
        'secondaryContactEmailRequirement',
        'siteOwnerRequirement',
        'interestRequirement',
        'projectManagerRequirement',
        'siteStatusRequirement',
        'legacyProjectCodeRequirement',
        'planningPermissionReferenceRequirement',
        'developmentLiabilityOrganisationRequirement',
        'postCompletionLiabilityOrganisationRequirement'
      ];
    }
  };
}

angular.module('GLA')
  .service('ProjectDetailsService', ProjectDetailsService);
