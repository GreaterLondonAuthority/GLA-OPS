import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import {FundingSourceTableComponent} from './funding-source-table.component';

describe('FundingSourceTableComponent', () => {
  let component: FundingSourceTableComponent;
  let fixture: ComponentFixture<FundingSourceTableComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ FundingSourceTableComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(FundingSourceTableComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
