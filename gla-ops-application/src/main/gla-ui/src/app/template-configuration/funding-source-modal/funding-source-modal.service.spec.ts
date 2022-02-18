import {TestBed} from '@angular/core/testing';

import {FundingSourceModalService} from './funding-source-modal.service';

describe('FundingSourceModalService', () => {
  let service: FundingSourceModalService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(FundingSourceModalService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
