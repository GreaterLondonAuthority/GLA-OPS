import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { FundingClaimModalComponent } from './funding-claim-modal.component';

describe('FundingClaimModalComponent', () => {
  let component: FundingClaimModalComponent;
  let fixture: ComponentFixture<FundingClaimModalComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ FundingClaimModalComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(FundingClaimModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
