import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { FundingActivitiesClaimModalComponent } from './funding-activities-claim-modal.component';

describe('FundingActivitiesClaimModalComponent', () => {
  let component: FundingActivitiesClaimModalComponent;
  let fixture: ComponentFixture<FundingActivitiesClaimModalComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ FundingActivitiesClaimModalComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(FundingActivitiesClaimModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
