/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

class GrantSourceChangeReport {
  constructor(GrantSourceService) {
    this.GrantSourceService = GrantSourceService;
  }

  $onInit(){
    this.GrantSourceService.getSourceVisibilityConfig(this.data.context.template).then((resp) => {
      this.config = resp;
      this.blockMetaData = this.GrantSourceService.getBlockMetaData(this.data.context.template);
      this.config = this.GrantSourceService.getAssociatedVisibilityConfig(this.data, this.config);
      [this.data.left, this.data.right].forEach((block)=>{
        if(block) {
          block.totalAmountRequested = this.GrantSourceService.getTotal(block);
        }
      });
    });

  }
}

GrantSourceChangeReport.$inject = ['GrantSourceService'];

angular.module('GLA')
  .component('grantSourceChangeReport', {
    bindings: {
      data: '<'
    },
    templateUrl: 'scripts/pages/change-report/blocks/grantSourceChangeReport.html',
    controller: GrantSourceChangeReport  });
