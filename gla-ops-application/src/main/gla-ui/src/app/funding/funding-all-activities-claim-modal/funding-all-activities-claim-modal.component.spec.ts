import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { FundingAllActivitiesClaimModalComponent } from './funding-all-activities-claim-modal.component';

describe('FundingAllActivitiesClaimModalComponent', () => {
  let component: FundingAllActivitiesClaimModalComponent;
  let fixture: ComponentFixture<FundingAllActivitiesClaimModalComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ FundingAllActivitiesClaimModalComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(FundingAllActivitiesClaimModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
