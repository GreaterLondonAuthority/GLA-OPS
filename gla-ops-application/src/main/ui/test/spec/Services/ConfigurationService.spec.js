/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

describe('Service: ConfigurationService', () => {

  beforeEach(angular.mock.module('GLA'));

  let ConfigurationService, $httpBackend;

  beforeEach(inject($injector => {
    ConfigurationService = $injector.get('ConfigurationService');
    $httpBackend = $injector.get('$httpBackend');
  }));

  describe('comingSoonMessage', () => {
    let comingSoonUrlRegEx;

    beforeEach(() => {
      comingSoonUrlRegEx = /.*\/messages\/coming-soon.*/g;
    });

    it('should return valid message for content without restricted words', (done) => {
      let comingSoonMsg = 'Valid msg';
      $httpBackend.expectGET(comingSoonUrlRegEx).respond(comingSoonMsg);

      ConfigurationService.comingSoonMessage().then(rsp =>{
        expect(rsp.data).toBe(comingSoonMsg);
        done();
      });

      $httpBackend.flush();
    });

    it('should return null for content with restricted words', (done) => {
      let comingSoonMsg = '<body>Outage Page</body>';
      $httpBackend.expectGET(comingSoonUrlRegEx).respond(comingSoonMsg);

      ConfigurationService.comingSoonMessage().then(rsp =>{
        expect(rsp.data).toBeNull();
        done();
      });

      $httpBackend.flush();
    });
  });

});

