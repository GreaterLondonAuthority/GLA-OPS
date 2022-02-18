import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { QuarterlyBudgetTableComponent } from './quarterly-budget-table.component';

describe('QuarterlyBudgetTableComponent', () => {
  let component: QuarterlyBudgetTableComponent;
  let fixture: ComponentFixture<QuarterlyBudgetTableComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ QuarterlyBudgetTableComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(QuarterlyBudgetTableComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
