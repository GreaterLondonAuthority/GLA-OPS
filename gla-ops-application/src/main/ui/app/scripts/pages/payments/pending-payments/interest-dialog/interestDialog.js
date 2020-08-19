/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

function InterestDialog($uibModal, _) {
  return {
    show(group) {
      return $uibModal.open({
        bindToController: true,
        controllerAs: '$ctrl',
        animation: false,
        templateUrl: 'scripts/pages/payments/pending-payments/interest-dialog/interestDialog.html',
        size: 'confirm',
        controller: [
          function() {

            let interests = [];
            _.forEach(group.payments, (payment) => {
              if(payment.reclaim && !payment.interestPayment){
                let interest = payment.interest;
                if(_.isNumber(interest)){
                  if(interest <= 0){
                    interest = -1 * interest;
                  } else {
                    throw new Error('Expected interest to a negative number that is then flipped here');
                  }
                }
                interests.push({
                  id: payment.id,
                  interest: interest,
                  paymentLabel: payment.subCategory + ' - ' + payment.source
                });

              }
            });

            this.interests = interests;

            this.update = () => {

              this.$close(this.interests);
            };
            this.enableUpdate = () => {
              let allHaveValues = true;
              _.forEach(this.interests, (item)=>{
                if(!_.isNumber(item.interest)){
                  allHaveValues = false;
                }
              });
              return allHaveValues;
            }
          }
        ]
      });
    }
  }
}

InterestDialog.$inject = ['$uibModal', '_'];

angular.module('GLA')
  .service('InterestDialog', InterestDialog);
