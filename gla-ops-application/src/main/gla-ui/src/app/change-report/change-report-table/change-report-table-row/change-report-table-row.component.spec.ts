import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { ChangeReportTableRowComponent } from './change-report-table-row.component';

describe('ChangeReportTableRowComponent', () => {
  let component: ChangeReportTableRowComponent;
  let fixture: ComponentFixture<ChangeReportTableRowComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ ChangeReportTableRowComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ChangeReportTableRowComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
