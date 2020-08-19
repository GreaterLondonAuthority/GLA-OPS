/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */


class SkillProfilesTableCtrl {
  constructor($state,  SkillProfilesService, UserService) {
    this.$state = $state;
    this.SkillProfilesService = SkillProfilesService;
    this.UserService = UserService;
 }

  $onInit(){
    this.id = this.paymentType.replace('_', '-').toLowerCase()
  }

  isRowValid(entry) {
    return this.totals[entry] === '100.00';
  }

  isEveryRowValid() {
    return !_.some(Object.keys(this.totals || {}), key =>  +(this.totals[key]) != 100 );
  }

  isRowDeletable(entry) {
    return this.data[entry].filter(profile => profile.editable === true).length == 12;
  }
}

SkillProfilesTableCtrl.$inject = ['$state', 'SkillProfilesService', 'UserService'];

angular.module('GLA')
  .component('skillProfilesTable', {
    templateUrl: 'scripts/pages/system/skill-profiles/skill-profiles-table/skillProfilesTable.html',
    bindings: {
      readOnly: '<',
      data: '<',
      totals: '<',
      title: '<',
      paymentType: '<',
      onCreate: '&',
      onUpdate: '&',
      onDelete: '&'
    },
    controller: SkillProfilesTableCtrl
  });
