/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

function JSONViewerDialog($uibModal, _, TemplateService) {
  return {
    show(config) {
      var defaultConfig = {
        title: false,
        approveText: 'Yes',
        dismissText: 'No',
        showApprove: true,
        showDismiss: true,
        message: 'Are you sure?',
        info: false,
        data: '',
        textAreaLabel: false,
        readonly: true
      };

      return $uibModal.open({
        bindToController: true,
        controllerAs: '$ctrl',
        animation: false,
        templateUrl: 'scripts/components/common/json-viewer-dialog/JSONViewerDialog.html',
        size: 'lg',
        windowClass: 'json-viewer-dialog',
        controller: [function() {
          this.config = _.merge(defaultConfig, config);
          this.onAdd = (data) => {
            return TemplateService.createTemplate(data).toPromise().then(resp => {
              this.$close();
              return this.$state.go(this.$state.current, this.$state.params, {reload: true});
            }, (resp)=>{
              this.errorMsg = resp.data.description;

            });
          }
        }]
      });
    },

    create() {
      let config = {
        title: 'Create a template',
        textAreaLabel: 'Add JSON code to create a new template.',
        approveText: 'ADD',
        showApprove: false,
        showDismiss: false,
        showAddBtn: true,
        readonly: false
      }
      return this.show(config).result;
    }
  }
}

JSONViewerDialog.$inject = ['$uibModal', '_', 'TemplateService'];

angular.module('GLA')
  .service('JSONViewerDialog', JSONViewerDialog);
