import { TestBed } from '@angular/core/testing';

import { OutputCategoryService } from './output-category.service';

describe('OutputCategoryService', () => {
  let service: OutputCategoryService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(OutputCategoryService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
