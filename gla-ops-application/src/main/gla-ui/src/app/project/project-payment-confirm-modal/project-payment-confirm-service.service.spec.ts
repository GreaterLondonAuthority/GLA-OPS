import { TestBed } from '@angular/core/testing';

import { ProjectPaymentConfirmService } from './project-payment-confirm.service';

describe('ProjectPaymentConfirmServiceService', () => {
  let service: ProjectPaymentConfirmService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ProjectPaymentConfirmService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
