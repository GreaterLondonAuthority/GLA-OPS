/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
import NumberUtil from '../../../../util/NumberUtil';

const textsByStatus = {
  claim: `By claiming the outputs, you are confirming the outputs have been delivered. Claimed payments will display in the Payments section of OPS once the project changes have been approved`,
  claimZero: `You are confirming that the outputs have not been delivered`,
  claimed: 'Output claims must be cancelled before the outputs can be edited. By cancelling this claim, all unapproved claims will also be cancelled',
  approved: null
};

let gla = angular.module('GLA');

class FinancialYearQuarterlyOutputsTable {
  constructor(OutputsService, ClaimModal, fYearFilter) {
    this.OutputsService = OutputsService;
    this.ClaimModal = ClaimModal;
    this.fYearFilter = fYearFilter;
  }

  $onInit() {
    this.OutputsService.getOutputConfigGroup(this.blockConfig.outputConfigurationGroup.id).then((resp) => {
      let recoveryCategory = _.find(resp.data.categories, {id: this.recoveryOutputId});
      this.selectedRecoveryOutputId = (recoveryCategory || {}).id;
    });
  }

  showQuarterlyClaimModal(quarter, claim) {
    claim = claim || {};
    let claimStatus = claim.claimStatus || 'Claim';
    let isClaimed = (claim.claimStatus === 'Claimed' || claim.claimStatus === 'Approved');

    let text = textsByStatus[claimStatus.toLowerCase()];
    if(claimStatus == 'Claim' && !quarter.actualTotal){
      text = textsByStatus.claimZero;
    }

    let config = {
      title: `${claimStatus.toUpperCase()} OUTPUTS`,
      subtitle: `Q${quarter.quarter} ${this.fYearFilter(this.financialYear)} Outputs`,
      text: text,
      claimableAmount: quarter.claimableAmountTotal || 0,
      isClaimed: isClaimed,
      cancelBtnText: quarter.actualTotal? `CANCEL Q${quarter.quarter} PAYMENT` : `CANCEL Q${quarter.quarter} CLAIM`,
      claimBtnText: quarter.actualTotal? `CLAIM Q${quarter.quarter} PAYMENT` : 'CONFIRM',
      readOnly: this.readOnly || claim.claimStatus === 'Approved'
    };

    let claimRequest = {
      id: claim.id,
      projectId: this.projectId,
      blockId: this.blockId,
      year: this.financialYear,
      claimTypePeriod: quarter.quarter,
    };

    let modal = this.ClaimModal.show(config, claimRequest);
    modal.result.then((result) => {
      let event = {event: quarter};
      return (result === 'claim')? this.onClaim(event) : this.onCancelClaim(event);
    });

  }

  /**
   * Format number to string with comma's and append CR
   * @see `NumberUtil.formatWithCommasAndCR()`
   */
  formatNumber(value) {
    if (value) {
      return NumberUtil.formatWithCommas(value, 2);
    }
  }

  populateAdvancePaymentColumn(remainingAdvancePayment, categoryId) {
    if (remainingAdvancePayment > 0 && this.advancePaymentStatus === 'Approved' && this.selectedRecoveryOutputId === categoryId){
      return this.formatNumber(remainingAdvancePayment);
    } else if (this.selectedRecoveryOutputId === categoryId){
      return 0;
    } else {
      return '';
    }
  }

  isQuarterEditable(quarter) {
    return !(this.readOnly || quarter.claim);
  }

  monthName(month) {
    return moment().month(month - 1).format('MMM');
  }

  /**
   * Empty claimable quarter is a quarter which will become claimed automatically if next quarter will be claimed
   */
  isEmptyClaimableQuarter(quarter){
    if(quarter.outputsMonths.length || (!this.nextClaimableQuarter && !this.latestClaim)){
      return false;
    }

    if(this.latestClaim){
      return quarter.year < this.latestClaim.year ||
        (quarter.year === this.latestClaim.year && quarter.quarter < this.latestClaim.claimTypePeriod);
    }

    return false;
  }

  getSubcategory(item){
    if(item.config.subcategory && item.config.subcategory != 'N/A'){
      return item.config.subcategory;
    }
  }
}

FinancialYearQuarterlyOutputsTable.$inject = ['OutputsService', 'ClaimModal', 'fYearFilter'];

gla.component('financialYearQuarterlyOutputsTable', {
  bindings: {
    financialYear: '<',
    projectId: '<',
    blockId: '<',
    categoryTitle: '<',
    tableData: '<',
    blockConfig: '<',
    readOnly: '<',
    onRowChanged: '&',
    onDelete: '&',
    onClaim: '&',
    onCancelClaim: '&',
    selectedYear: '<',
    recoveryOutputId: '<',
    claimable: '<',
    showAdvancedPaymentColumn: '<',
    advancePaymentStatus: '<',
    showAdvancePaymentColumn: '<',
    latestClaim: '<'
  },
  templateUrl: 'scripts/pages/project/outputs/financial-year-quarterly-outputs-table/financialYearQuarterlyOutputsTable.html',
  controller: FinancialYearQuarterlyOutputsTable
});
