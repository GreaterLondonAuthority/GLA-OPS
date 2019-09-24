/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

import ProjectBlockCtrl from '../ProjectBlockCtrl';

class OutputsCostsCtrl extends ProjectBlockCtrl {
  constructor(project, $injector, $timeout, ProjectBlockService, OutputsService) {
    super($injector);
    this.$timeout = $timeout;
    this.ProjectBlockService = ProjectBlockService;
    this.OutputsService = OutputsService;
  }

  $onInit() {
    super.$onInit();
    this.outputsBlock = (_.find(this.project.projectBlocksSorted, {type: 'OutputsBlock'})  || {});
    this.outputCategories = [];
    this.OutputsService.getOutputConfigGroup(this.outputsBlock.configGroupId).then((resp) => {
      this.outputCategories = resp.data.categories;
      // init table
      _.each(this.outputCategories, category => {
        if (!this.getOutputCost(category.id)) {
          this.projectBlock.categoriesCosts.push({
            outputCategoryConfigurationId: category.id
          });
        }
      });
    });
  }

  getOutputCost(categoryId) {
    return _.find(this.projectBlock.categoriesCosts, {outputCategoryConfigurationId: categoryId});
  }

  getOutputCategoryName(categoryId) {
    return (_.find(this.outputCategories, {id: categoryId}) || {}).category;
  }

  back() {
    this.returnToOverview();
  }

  onSaveData(releaseLock) {
    let p = this.ProjectBlockService.updateBlock(this.project.id, this.projectBlock.id, this.projectBlock, releaseLock);
    return this.addToRequestsQueue(p);
  }

  autoSave() {
    this.onSaveData(false).then(resp => {
        this.projectBlock = resp.data;
    });
  }

  submit() {
    return this.$timeout(() => {
      return this.$q.all(this.requestsQueue).then(results => {
        return this.onSaveData(true);
      });
    });
  }
}

OutputsCostsCtrl.$inject = ['project', '$injector', '$timeout', 'ProjectBlockService', 'OutputsService'];

angular.module('GLA')
  .controller('OutputsCostsCtrl', OutputsCostsCtrl);
