/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

'use strict';

describe('Controller: WbsCodesCtrl', () => {

  beforeEach(angular.mock.module('GLA'));

  let ctrl, $componentController;

  beforeEach(inject(function (_$componentController_) {
    $componentController = _$componentController_;
  }));

  beforeEach(() => $ctrl([
    {code: 'code1', spendType: 'REVENUE'},
    {code: 'code2', spendType: 'REVENUE'},
    {code: 'code3', spendType: 'CAPITAL'},
    {code: 'code4', spendType: 'CAPITAL'}
  ], 'CAPITAL'));


  describe('delete', () => {
    it('delete existing', () => {
      ctrl.delete({code: 'code1'});
      expect(_.find(ctrl.codes, {code: 'code1'})).toBeFalsy();
    });
  });

  describe('add', () => {
    it('adds wbs code to the list', () => {
      ctrl.add('code4');
      expect(_.find(ctrl.codes, {code: 'code4'})).toEqual({code:'code4', spendType: 'CAPITAL'});
    });
  });


  function $ctrl(codes, type, readOnly){
    let bindings = {
      codes: codes,
      type: type,
      readOnly: readOnly
    };
    ctrl =  $componentController('wbsCodes', null, bindings);
  }
});
