import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { RequestPasswordResetPageComponent } from './request-password-reset-page.component';

describe('RequestPasswordResetPageComponent', () => {
  let component: RequestPasswordResetPageComponent;
  let fixture: ComponentFixture<RequestPasswordResetPageComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ RequestPasswordResetPageComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RequestPasswordResetPageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
