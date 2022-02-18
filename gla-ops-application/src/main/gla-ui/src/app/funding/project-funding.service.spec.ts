import { TestBed } from '@angular/core/testing';

import { ProjectFundingService } from './project-funding.service';

describe('ProjectFundingService', () => {
  let service: ProjectFundingService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ProjectFundingService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
