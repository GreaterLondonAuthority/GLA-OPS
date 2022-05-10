import {ComponentFixture, TestBed} from '@angular/core/testing';

import {ContractWithdrawModalComponent} from './contract-withdraw-modal.component';

describe('ContractWithdrawModalComponent', () => {
  let component: ContractWithdrawModalComponent;
  let fixture: ComponentFixture<ContractWithdrawModalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ContractWithdrawModalComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ContractWithdrawModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
