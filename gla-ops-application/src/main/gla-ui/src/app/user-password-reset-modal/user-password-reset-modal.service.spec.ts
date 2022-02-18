import { TestBed } from '@angular/core/testing';

import { UserPasswordResetModalService } from './user-password-reset-modal.service';

describe('UserPasswordResetModalService', () => {
  let service: UserPasswordResetModalService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(UserPasswordResetModalService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
