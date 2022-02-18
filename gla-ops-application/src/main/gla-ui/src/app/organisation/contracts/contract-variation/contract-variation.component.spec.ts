import {ComponentFixture, TestBed} from '@angular/core/testing';

import {ContractVariationComponent} from './contract-variation.component';

describe('ContractVariationComponent', () => {
  let component: ContractVariationComponent;
  let fixture: ComponentFixture<ContractVariationComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ContractVariationComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ContractVariationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
