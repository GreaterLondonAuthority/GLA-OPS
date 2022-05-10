import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import {PaymentAuthorisationModalComponent} from './payment-authorisation-modal.component';

describe('PaymentAuthorisationModalComponent', () => {
  let component: PaymentAuthorisationModalComponent;
  let fixture: ComponentFixture<PaymentAuthorisationModalComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ PaymentAuthorisationModalComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PaymentAuthorisationModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
