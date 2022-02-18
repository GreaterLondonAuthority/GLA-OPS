import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { OrganisationSapIdModalComponent } from './organisation-sap-id-modal.component';

describe('OrganisationSapIdModalComponent', () => {
  let component: OrganisationSapIdModalComponent;
  let fixture: ComponentFixture<OrganisationSapIdModalComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ OrganisationSapIdModalComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(OrganisationSapIdModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
