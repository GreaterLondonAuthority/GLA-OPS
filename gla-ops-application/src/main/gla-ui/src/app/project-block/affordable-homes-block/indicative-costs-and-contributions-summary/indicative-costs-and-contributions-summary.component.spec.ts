import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { IndicativeCostsAndContributionsSummaryComponent } from './indicative-costs-and-contributions-summary.component';

describe('IndicativeCostsAndContributionsSummaryComponent', () => {
  let component: IndicativeCostsAndContributionsSummaryComponent;
  let fixture: ComponentFixture<IndicativeCostsAndContributionsSummaryComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ IndicativeCostsAndContributionsSummaryComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(IndicativeCostsAndContributionsSummaryComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
