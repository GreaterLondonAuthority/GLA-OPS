import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { MobileDeviceWarningComponent } from './mobile-device-warning.component';

describe('MobileDeviceWarningComponent', () => {
  let component: MobileDeviceWarningComponent;
  let fixture: ComponentFixture<MobileDeviceWarningComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ MobileDeviceWarningComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MobileDeviceWarningComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
