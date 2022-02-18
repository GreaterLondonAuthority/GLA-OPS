import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { AffordableHomesChangeReportComponent } from './affordable-homes-change-report.component';

describe('IndicativeStartsAndCompletionsChangeReportComponent', () => {
  let component: AffordableHomesChangeReportComponent;
  let fixture: ComponentFixture<AffordableHomesChangeReportComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ AffordableHomesChangeReportComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AffordableHomesChangeReportComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
