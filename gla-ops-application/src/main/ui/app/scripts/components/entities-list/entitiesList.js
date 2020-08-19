/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

import './entities-list-item/entitiesListItem'

const gla = angular.module('GLA');

class EntitiesList {

  constructor(ConfirmationDialog, RepeatingEntityService, ErrorService) {
    this.ConfirmationDialog = ConfirmationDialog;
    this.RepeatingEntityService = RepeatingEntityService;
    this.ErrorService = ErrorService;
  }

  $onInit() {
    if(this.entities && !this.entities.length){
      this.entities.push({});
    }
  }

  addEmptyEntity(){
    this.entities.push({});
  }

  addEntity(entity) {

    this.RepeatingEntityService.create(this.block.rootPath, this.block.projectId, this.block.id, entity).then(rsp => {
      _.merge(entity, rsp.data);
    }).catch(this.ErrorService.apiValidationHandler());
  }

  updateEntity(entity){
    this.RepeatingEntityService.update(this.block.rootPath, this.block.projectId, this.block.id, entity).then(rsp => {
      _.merge(entity, rsp.data);
    }).catch(this.ErrorService.apiValidationHandler());
  }

  deleteEntity(entity){
    let modal = this.ConfirmationDialog.delete();
    modal.result.then(resp => {
      if(entity.id) {
        _.remove(this.entities, entity);
        this.RepeatingEntityService.delete(this.block.rootPath, this.block.projectId, this.block.id, entity).then(rsp => {
        }).catch(this.ErrorService.apiValidationHandler());
      }else{
        _.remove(this.entities, entityInList => {
          return entityInList === entity;
        });
      }
    });
  }

  isAddButtonDisabled(){
    let lastEntity = this.entities[this.entities.length - 1];
    if(lastEntity && lastEntity.id) {
      return false;
    }

    return !this.hasEntityAnyData(lastEntity);
  }

  hasEntityAnyData(entity){
    return this.RepeatingEntityService.hasEntityAnyData(entity)
  }
}

EntitiesList.$inject = ['ConfirmationDialog', 'RepeatingEntityService', 'ErrorService'];


gla.component('entitiesList', {
  templateUrl: 'scripts/components/entities-list/entitiesList.html',
  controller: EntitiesList,
  transclude: {
    entitiesListItem: '?entitiesListItem'
  },
  bindings: {
    entities: '<',
    block: '<',
    readOnly: '<'
  },
});

