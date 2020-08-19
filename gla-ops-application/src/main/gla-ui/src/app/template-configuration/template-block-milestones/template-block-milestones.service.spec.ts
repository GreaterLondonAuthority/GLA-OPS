import {TestBed} from '@angular/core/testing';

import {TemplateBlockMilestonesService} from './template-block-milestones.service';

describe('TemplateBlockMilestonesService', () => {
  let service: TemplateBlockMilestonesService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(TemplateBlockMilestonesService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
