import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { RegistrationTypePageComponent } from './registration-type-page.component';

describe('RegistrationTypePageComponent', () => {
  let component: RegistrationTypePageComponent;
  let fixture: ComponentFixture<RegistrationTypePageComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ RegistrationTypePageComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RegistrationTypePageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
