/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

function ProjectOtherFundingModal($uibModal) {
  return {
    show: function (otherFunding, templateConfig) {
      return $uibModal.open({
        bindToController: true,
        controllerAs: '$ctrl',
        animation: false,
        templateUrl: 'scripts/pages/project/other-funding/projectOtherFundingModal.html',
        size: 'md',
        resolve: {

        },
        controller: ['$uibModalInstance', function ($uibModalInstance) {
          this.otherFunding = angular.copy(otherFunding || {});
          this.templateConfig = templateConfig;

          this.dateOptions = {
            showWeeks: false,
            format: 'dd/MM/yyyy',
            formatYear: 'yyyy',
            formatMonth: 'MMM',
            yearColumns: 3,
            initDate: new Date()
          };

          this.fundingSourcesNames = [];
          _.each(this.templateConfig.fundingSources, funding => {
            this.fundingSourcesNames.push(funding.fundingSource)
          });

          this.isDescriptionOn = (fundingSourceName)=>{
            if(fundingSourceName !== undefined) {
              let fundingSource = _.find(this.templateConfig.fundingSources, {fundingSource: fundingSourceName})
              return fundingSource.showDescription;
            } else {return false}
          };

          this.isFunderNameOn = (fundingSourceName)=>{
            if(fundingSourceName !== undefined) {
            let fundingSource = _.find(this.templateConfig.fundingSources, {fundingSource: fundingSourceName})
              return fundingSource.showFunderName;
            } else {return false}
          }

          this.isFundingValid = (otherFunding)=>{
            if((this.templateConfig.fundingSources.length != 0 && !otherFunding.fundingSource)
              || (this.isFunderNameOn(otherFunding.fundingSource) && !otherFunding.funderName)
              || (this.isDescriptionOn(otherFunding.fundingSource) && !otherFunding.description)
              || (this.templateConfig.showAmount && !otherFunding.amount)) {
              return false;
            }

            if(this.templateConfig.showSecuredQuestion && otherFunding.fundingSecured == undefined) {
              return false;
            } else {
              if(otherFunding.fundingSecured == true && !otherFunding.dateSecured) {
                return false;
              }
              if(otherFunding.fundingSecured == false && !otherFunding.estimateDateSecured) {
                return false;
              }
            }
            return true;
          }

        }]
      });
    },

  };
}

ProjectOtherFundingModal.$inject = ['$uibModal'];

angular.module('GLA')
.service('ProjectOtherFundingModal', ProjectOtherFundingModal);

