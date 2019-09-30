/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

import './skill-profiles-table/skillProfilesTable'

class SkillProfilesCtrl {
  constructor($state,  SkillProfilesService, UserService, ConfirmationDialog) {
    this.$state = $state;
    this.SkillProfilesService = SkillProfilesService;
    this.UserService = UserService;
    this.ConfirmationDialog = ConfirmationDialog;
    this.data = {};
    this.totals = {};
    this.readOnly;
    this.paymentTypes = [{
      id: 'AEB_GRANT',
      title: 'AEB Grant payment profiles percentages'
    }, {
      id: 'AEB_PROCURED',
      title: 'AEB Procured - Delivery profiles'
    }, {
      id: 'AEB_LEARNER_SUPPORT',
      title: 'AEB Procured - Learner support profiles'
    }];
  }

  $onInit(){
    this.readOnly = !this.$state.params.editMode;
    this.editable = this.UserService.hasPermission('admin.skill.profiles') && this.readOnly;
    this.getData();
  }

  createNewRow(type) {
    this.SkillProfilesService.createNewYearData(type).then(() => this.getData());
  }

  updateValue(value) {
    this.SkillProfilesService.updateValue(value).then(() => this.getData());
  }

  deleteValue(skillsProfile){
    let modal = this.ConfirmationDialog.delete('Are you sure you want to delete the allocation profile?');
    modal.result.then(() => {
      this.SkillProfilesService.deletePaymentProfilesByTypeAndYear(skillsProfile.type, skillsProfile.year).then(() => this.getData());
    });
  }

  getData() {
    this.SkillProfilesService.getSkillsPaymentProfiles().then(res => {
      this.transformData(res.data);
    });
  }

  getDateOptions(year){
    return {
      showWeeks: false
      // minDate: new Date(`${year}-08-01`),
      // maxDate: new Date(`${year + 1}-08-31`)
    }
  }


  transformData(apiData) {
    this.paymentTypes.forEach(pt => {
      let type = pt.id;
      this.totals[type] = null;
      let profiles = (apiData || []).filter(a => a.type === type);
      profiles.forEach(p =>  p.dateOptions = this.getDateOptions(p.year));
      this.data[type] = _.groupBy(profiles, 'year');
      for (let entry in this.data[type]) {
        this.data[type][entry].forEach(m => m.percentage = (m.percentage != null? m.percentage.toFixed(2) : null));
        this.data[type][entry] = _.sortBy(this.data[type][entry], 'period');
        this.totals[type] = this.totals[type] || {};
        this.totals[type][entry] = _.reduce(this.data[type][entry], (total, num) => {
          return total + +(num.percentage || 0);
        }, 0).toFixed(2);
      }
    });
  }

  edit(){
    this.readOnly = !this.readOnly;
  }

  back(){
    this.$state.go('system');
  }

  save() {
    this.readOnly = true;
  }

  stopEditing(){
    this.save();
  }
}

SkillProfilesCtrl.$inject = ['$state', 'SkillProfilesService', 'UserService', 'ConfirmationDialog'];

angular.module('GLA')
  .component('skillProfiles', {
    templateUrl: 'scripts/pages/system/skill-profiles/skillProfiles.html',
    controller: SkillProfilesCtrl
  });
