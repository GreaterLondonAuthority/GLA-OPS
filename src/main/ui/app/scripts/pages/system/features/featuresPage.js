/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */


class FeaturesPageCtrl {
  constructor($state, FeatureToggleService, UserService) {
    this.$state = $state;
    this.FeatureToggleService = FeatureToggleService;
    this.UserService = UserService;
  }

  $onInit(){
    this.readOnly = !this.$state.params.editMode;
    this.editable = this.UserService.hasPermission('system.features.edit') && this.readOnly;
  }

  edit(){
    this.readOnly = !this.readOnly;
  }

  back(){
    this.$state.go('system');
  }

  toggle(feature){
    this.FeatureToggleService.updateFeature(feature.name, !feature.enabled).then(rsp => {
      _.merge(feature, rsp.data);
    });
  }

  save(){
    this.readOnly = true;
  }
}

FeaturesPageCtrl.$inject = ['$state', 'FeatureToggleService', 'UserService'];

angular.module('GLA')
  .component('featuresPage', {
    templateUrl: 'scripts/pages/system/features/featuresPage.html',
    bindings: {
      features: '<'
    },
    controller: FeaturesPageCtrl
  });
