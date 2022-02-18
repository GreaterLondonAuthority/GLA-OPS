import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { CookieWarningComponent } from './cookie-warning.component';

describe('CookieWarningComponent', () => {
  let component: CookieWarningComponent;
  let fixture: ComponentFixture<CookieWarningComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ CookieWarningComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CookieWarningComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
