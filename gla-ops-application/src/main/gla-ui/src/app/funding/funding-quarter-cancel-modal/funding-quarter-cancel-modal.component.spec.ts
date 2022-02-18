import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FundingQuarterCancelModalComponent } from './funding-quarter-cancel-modal.component';

describe('FundingQuarterCancelModalComponent', () => {
  let component: FundingQuarterCancelModalComponent;
  let fixture: ComponentFixture<FundingQuarterCancelModalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ FundingQuarterCancelModalComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(FundingQuarterCancelModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
