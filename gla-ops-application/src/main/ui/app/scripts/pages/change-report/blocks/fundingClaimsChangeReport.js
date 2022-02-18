/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */


class FundingClaimsChangeReport {
  constructor(ReportService, Util, TemplateService, ProjectSkillsService) {
    this.ReportService = ReportService;
    this.NumberUtil = Util.Number;
    this.TemplateService = TemplateService;
    this.ProjectSkillsService = ProjectSkillsService;
  }


  $onInit() {
    let block = this.data.right || this.data.left;
    const template = this.data.context.template;
    let templateConfig = this.TemplateService.getBlockConfig(template, block);
    this.isAebProcured = this.ProjectSkillsService.isAebProcured(block);
    this.isAebGrant = this.ProjectSkillsService.isAebGrant(block);

    let contractTypes = this.getActiveContractTypes(this.data, templateConfig);
    this.contractTypes = contractTypes;

    let periodsMap = this.ProjectSkillsService.getPeriodIdToTextMap(templateConfig.periods);


    let comparableFundingClaimRows = this.getComparableFundingClaimRows(this.data.left, this.data.right);
    this.comparableFundingClaimGroups = this.ReportService.groupComparableRows(comparableFundingClaimRows, 'year', true);


    this.aebGrantFields = [
      {
        field: 'period',
        label: 'FUNDING CLAIM PERIOD',
        format(row){
          return row ? periodsMap[row.period] : null;
        }
      },
      {
        field: 'actualTotal',
        label: 'TOTAL DELIVERY TO DATE £',
        format(row){
          return row && row.totals ? row.totals.actualTotal : null;
        }
      },
      {
        field: 'forecastTotal',
        label: 'TOTAL FORECAST DELIVERY £',
        format(row){
          return row && row.totals ? row.totals.forecastTotal : null;
        }
      },
      {
        field: 'deliveryTotal',
        label: 'TOTAL DELIVERY £',
        format(row){
          if(row && row.totals){
            return row.period != 14 ? row.totals.deliveryTotal : 'N/A'
          }
          return null;
        }
      }
    ];

    this.aebProcuredFields = [{
      field: 'period',
      label: 'FUNDING CLAIM PERIOD',
      format(row){
        return row ? periodsMap[row.period] : null;
      }
    }];

    contractTypes.forEach(ct => {
      this.aebProcuredFields.push({
        field: `${ct}:funding`,
        label: `TOTAL ${ct} £`,
        format(row){
          if(row && row.totals){
            let ctt = _.find(row.totals.contractTypeTotals, {contractType: ct});
            if(ctt){
              return ctt.funding;
            }
            return 'N/A'
          }
          return null;
        }
      });

      this.aebProcuredFields.push({
        field: `${ct}:flexibleFunding`,
        label: 'TOTAL OF WHICH, FLEXIBLE ALLOCATION',
        format(row){
          if(row && row.totals){
            let ctt = _.find(row.totals.contractTypeTotals, {contractType: ct});
            if(ctt){
              return `${ctt.flexibleFunding} (${ctt.percentage || 0}%)`;
            }
            return 'N/A'
          }
          return null;
        }
      });
    });

    if (contractTypes.length > 1) {

      this.aebProcuredFields.push({
        field: 'contractValueTotal',
        label: 'TOTAL DELIVERY £',
        format(row){
          return row && row.totals ? row.totals.contractValueTotal : null;
        }
      });

      this.aebProcuredFields.push({
        field: 'flexibleTotal',
        label: 'OF WHICH, FLEXIBLE ALLOCATION',
        format(row){
          return row && row.totals ? `${row.totals.flexibleTotal} (${row.totals.percentage || 0}%)` : null;
        }
      });
    }

    this.fields = this.isAebGrant ? this.aebGrantFields : this.aebProcuredFields;
  }

  getComparableFundingClaimRows(leftBlock, rightBlock) {
    let leftAndRightTotals = [leftBlock, rightBlock].map(block => {
      if (block && block.totals) {
        let rows = this.ProjectSkillsService.getPeriodTotals(block.totals, this.contractTypes.map(ct => {
          return {name: ct};
        }));

        return this.isAebProcured? rows.filter(row => row.totals.contractTypeTotals.length) : rows;
      }
      return [];
    });

    console.log('leftAndRightTotals', leftAndRightTotals)



    let leftSideFilter = function (rightRow) {
      return {year: rightRow.year, period: rightRow.period}
    };

    return this.ReportService.rowsToCompare(leftAndRightTotals[0], leftAndRightTotals[1], leftSideFilter);
  }

  getActiveContractTypes(data, templateConfig) {
    let leftSideActiveContractTypes = this.ProjectSkillsService.getActiveContractTypes(data.left, templateConfig);
    let rightSideActiveContractTypes = this.ProjectSkillsService.getActiveContractTypes(data.right, templateConfig);
    return _.unionBy(leftSideActiveContractTypes, rightSideActiveContractTypes, 'name').map(ct => ct.name);
  }
}

FundingClaimsChangeReport.$inject = ['ReportService', 'Util', 'TemplateService', 'ProjectSkillsService'];

angular.module('GLA')
  .component('fundingClaimsChangeReport', {
    bindings: {
      data: '<'
    },
    templateUrl: 'scripts/pages/change-report/blocks/fundingClaimsChangeReport.html',
    controller: FundingClaimsChangeReport
  });
