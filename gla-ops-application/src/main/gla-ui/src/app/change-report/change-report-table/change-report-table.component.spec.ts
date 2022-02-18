import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { ChangeReportTableComponent } from './change-report-table.component';

describe('ChangeReportTableComponent', () => {
  let component: ChangeReportTableComponent;
  let fixture: ComponentFixture<ChangeReportTableComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ ChangeReportTableComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ChangeReportTableComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
