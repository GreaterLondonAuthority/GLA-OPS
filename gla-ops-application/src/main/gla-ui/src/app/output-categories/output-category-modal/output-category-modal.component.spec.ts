import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import {OutputCategoryModalComponent} from './payment-authorisation-modal.component';

describe('PaymentAuthorisationModalComponent', () => {
  let component: OutputCategoryModalComponent;
  let fixture: ComponentFixture<OutputCategoryModalComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ OutputCategoryModalComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(OutputCategoryModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
