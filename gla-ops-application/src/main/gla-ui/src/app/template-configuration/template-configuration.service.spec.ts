import {TestBed} from '@angular/core/testing';

import {TemplateConfigurationService} from './template-configuration.service';

describe('TemplateConfigurationService', () => {
  let service: TemplateConfigurationService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(TemplateConfigurationService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
