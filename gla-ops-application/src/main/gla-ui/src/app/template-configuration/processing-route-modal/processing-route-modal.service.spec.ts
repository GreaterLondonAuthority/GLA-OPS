import {TestBed} from '@angular/core/testing';

import {ProcessingRouteModalService} from './processing-route-modal.service';

describe('ProcessingRouteModalService', () => {
  let service: ProcessingRouteModalService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ProcessingRouteModalService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
