/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

import NumberUtil from '../../util/NumberUtil';

function ActualsMetadataModal($uibModal) {
  return {
    show: function (dataPromise, title, spendType, isCR) {
      return $uibModal.open({
        bindToController: true,
        controllerAs: '$ctrl',
        animation: false,
        templateUrl: 'scripts/components/actuals-metadata-modal/actualsMetadataModal.html',
        size: 'lg',
        resolve: {
          data: () => {
            return dataPromise;
          }
        },
        controller: ['data', function (data) {
          // console.log('resolved data:', JSON.stringify(data, null, 4))
          this.title = title || 'More Info';
          this.data = data;
          this.isCRFormated = !!spendType;
          let i = 0;
          this.data.forEach(item => {
            if (item.date) {
              item.date = moment(item.date, 'DD/M/YYYY').toDate();
            }
          });
          this.source = {
            PCS: 'PCS Import',
            WebUI: 'Manual'
          };
          /**
           * Format number to string with comma's and append CR
           * @see `NumberUtil.formatWithCommasAndCR()`
           */
          this.formatNumberWithCR = (value) => {
            return NumberUtil.formatWithCommasAndCR(value, 2);
          };

          this.transactionFilter = (t) => {
            if (!spendType) {
              return true;
            }

            if (spendType && t.spendType === spendType) {
              return isCR ? t.amount < 0 : t.amount >= 0;
            }

            return false;
          }
        }]
      });
    }
  }


}

ActualsMetadataModal.$inject = ['$uibModal'];

angular.module('GLA')
  .service('ActualsMetadataModal', ActualsMetadataModal);
