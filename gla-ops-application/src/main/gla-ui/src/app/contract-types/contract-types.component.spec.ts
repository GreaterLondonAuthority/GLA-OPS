import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ContractTypesComponent } from './contract-types.component';

describe('ContractTypesComponent', () => {
  let component: ContractTypesComponent;
  let fixture: ComponentFixture<ContractTypesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ContractTypesComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ContractTypesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
