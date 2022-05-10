import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ProjectPaymentConfirmModalComponent } from './project-payment-confirm-modal.component';

describe('ProjectPaymentConfirmModalComponent', () => {
  let component: ProjectPaymentConfirmModalComponent;
  let fixture: ComponentFixture<ProjectPaymentConfirmModalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ProjectPaymentConfirmModalComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ProjectPaymentConfirmModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
