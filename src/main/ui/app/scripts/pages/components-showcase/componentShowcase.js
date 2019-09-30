/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */


class ComponentsShowcase {
  constructor() {
    this.blocks = [{
      name: 'DRAFT',
      icon: 'glyphicon-exclamation-sign',
      blockState: 'invalid',
      status: 'INCOMPLETE',
      theme: 'draft'
    }, {
      name: 'DRAFT',
      icon: 'glyphicon-ok',
      blockState: 'valid',
      status: 'SECTION COMPLETE',
      theme: 'draft'
    }, {
      name: 'ACTIVE',
      icon: 'glyphicon-exclamation-sign',
      blockState: 'invalid',
      status: 'UNAPPROVED',
    }, {
      name: 'ACTIVE',
      icon: 'glyphicon-ok',
      blockState: 'valid',
      status: 'APPROVED',
    }, {
      name: 'ACTIVE',
      icon: 'glyphicon-exclamation-sign',
      blockState: 'invalid',
      status: 'UNAPPROVED',
      banner: 'INCOMPLETE'
    }, {
      name: 'ACTIVE',
      icon: 'glyphicon-ok',
      blockState: 'valid',
      status: 'APPROVED',
      banner: 'INCOMPLETE'
    }, {
      name: 'ACTIVE',
      icon: 'glyphicon-exclamation-sign',
      blockState: 'invalid',
      status: 'INCOMPLETE',
      newBlock: true
    }, {
      name: 'ACTIVE LAND',
      icon: null,
      blockState: 'invalid',
      status: null,
      banner: 'INCOMPLETE'
    }, {
      name: 'ACTIVE LAND',
      icon: 'glyphicon-ok',
      blockState: 'valid',
      status: ''
    }];


    this.tiles =  [
      {
        name: 'Balance 2014/15',
        items: [{
          itemName: 'Opening balance',
          itemValue: '£3,000,000'
        }, {
          itemName: 'Total generated',
          itemValue: '£10,000,000',
          icon: 'glyphicon-plus'
        }, {
          itemName: 'Total spent',
          itemValue: '£0',
          icon: 'glyphicon-minus'
        }, {
          itemName: 'Closing balance',
          itemValue: '£13,000,000'
        }]
      },
      {
        name: 'Balance 2015/16',
        items: [{
          itemName: 'Opening balance',
          itemValue: '£3,000,000'
        }, {
          itemName: 'Total generated',
          itemValue: '£10,000,000',
          icon: 'glyphicon-plus'
        }, {
          itemName: 'Total spent',
          itemValue: '£0',
          icon: 'glyphicon-minus'
        }, {
          itemName: 'Closing balance',
          itemValue: '£13,000,000'
        }]
      },
      {
        name: 'Balance 2016/17',
        items: [{
          itemName: 'Opening balance',
          itemValue: '£3,000,000'
        }, {
          itemName: 'Total generated',
          itemValue: '£10,000,000',
          icon: 'glyphicon-plus'
        }, {
          itemName: 'Total spent',
          itemValue: '£0',
          icon: 'glyphicon-minus'
        }, {
          itemName: 'Closing balance',
          itemValue: '£13,000,000'
        }]
      }
    ]
  }
}

ComponentsShowcase.$inject = [];

angular.module('GLA')
  .controller('ComponentsShowcase', ComponentsShowcase);


angular.module('GLA')
  .component('componentsShowcase', {
    templateUrl: 'scripts/pages/components-showcase/componentsShowcase.html',
    bindings: {},
    controller: ComponentsShowcase
  });
