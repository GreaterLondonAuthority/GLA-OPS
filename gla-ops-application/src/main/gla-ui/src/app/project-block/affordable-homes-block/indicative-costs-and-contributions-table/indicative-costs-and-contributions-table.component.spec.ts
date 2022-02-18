import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { IndicativeCostsAndContributionsTableComponent } from './indicative-costs-and-contributions-table.component';

describe('IndicativeCostsAndContributionsTableComponent', () => {
  let component: IndicativeCostsAndContributionsTableComponent;
  let fixture: ComponentFixture<IndicativeCostsAndContributionsTableComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ IndicativeCostsAndContributionsTableComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(IndicativeCostsAndContributionsTableComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
