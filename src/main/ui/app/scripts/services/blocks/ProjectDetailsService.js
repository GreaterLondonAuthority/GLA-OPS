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
          placeholder: 'Organisation with grant liability during development'
        },

        postCompletionLiabilityOrganisationId: {
          label: 'Organisation with grant liability after completion',
          placeholder: 'Organisation with grant liability after completion'
        },

        description: {
          label: 'Enter a brief project description',
          placeholder: 'Provide an overview of project objectives and deliverables, max 1,000 characters'
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
          placeholder: 'First Line'
        },

        borough: {
          placeholder: 'Select a borough'
        },

        //TODO: became optional as default
        postcode: {
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
    }
  };
}

angular.module('GLA')
  .service('ProjectDetailsService', ProjectDetailsService);
