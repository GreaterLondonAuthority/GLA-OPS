import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { ChangeReportTableSeparatorComponent } from './change-report-table-separator.component';

describe('ChangeReportTableSeparatorComponent', () => {
  let component: ChangeReportTableSeparatorComponent;
  let fixture: ComponentFixture<ChangeReportTableSeparatorComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ ChangeReportTableSeparatorComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ChangeReportTableSeparatorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
