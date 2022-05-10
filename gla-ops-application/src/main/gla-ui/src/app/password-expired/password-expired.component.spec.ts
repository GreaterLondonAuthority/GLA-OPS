import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import {PasswordExpiredComponent} from './password-expired.component';

describe('PasswordExpiredComponent', () => {
  let component: PasswordExpiredComponent;
  let fixture: ComponentFixture<PasswordExpiredComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ PasswordExpiredComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PasswordExpiredComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
