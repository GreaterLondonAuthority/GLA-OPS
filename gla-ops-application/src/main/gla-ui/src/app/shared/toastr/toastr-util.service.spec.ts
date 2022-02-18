import {TestBed} from '@angular/core/testing';

import {ToastrUtilService} from './toastr-util.service';

describe('ToastrUtilService', () => {
  let service: ToastrUtilService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ToastrUtilService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
