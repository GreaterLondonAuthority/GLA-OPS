import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EmailReportsComponent } from './email-reports.component';

describe('EmailReportsComponent', () => {
  let component: EmailReportsComponent;
  let fixture: ComponentFixture<EmailReportsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ EmailReportsComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(EmailReportsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
