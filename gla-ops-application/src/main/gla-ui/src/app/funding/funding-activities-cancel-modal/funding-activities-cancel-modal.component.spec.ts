import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FundingActivitiesCancelModalComponent } from './funding-activities-cancel-modal.component';

describe('FundingActivitiesCancelModalComponent', () => {
  let component: FundingActivitiesCancelModalComponent;
  let fixture: ComponentFixture<FundingActivitiesCancelModalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ FundingActivitiesCancelModalComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(FundingActivitiesCancelModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
