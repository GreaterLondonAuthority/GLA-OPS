import { TestBed } from '@angular/core/testing';

import { OrganisationSapIdModalService } from './organisation-sap-id-modal.service';

describe('OrganisationSapIdModalService', () => {
  let service: OrganisationSapIdModalService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(OrganisationSapIdModalService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
