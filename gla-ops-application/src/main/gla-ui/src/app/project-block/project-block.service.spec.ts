import { TestBed } from '@angular/core/testing';

import { ProjectBlockService } from './project-block.service';

describe('ProjectBlockService', () => {
  let service: ProjectBlockService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ProjectBlockService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
