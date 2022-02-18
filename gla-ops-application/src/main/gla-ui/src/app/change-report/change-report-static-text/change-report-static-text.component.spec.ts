import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { ChangeReportStaticTextComponent } from './change-report-static-text.component';

describe('ChangeReportStaticTextComponent', () => {
  let component: ChangeReportStaticTextComponent;
  let fixture: ComponentFixture<ChangeReportStaticTextComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ ChangeReportStaticTextComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ChangeReportStaticTextComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
