import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { AffordableHomesSummaryReportComponent } from './affordable-homes-summary-report.component';

describe('IndicativeStartsAndCompletionsSummaryReportComponent', () => {
  let component: AffordableHomesSummaryReportComponent;
  let fixture: ComponentFixture<AffordableHomesSummaryReportComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ AffordableHomesSummaryReportComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AffordableHomesSummaryReportComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
