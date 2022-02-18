import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FinanceCategoryModalComponent } from './finance-category-modal.component';

describe('FinanceCategoryModalComponent', () => {
  let component: FinanceCategoryModalComponent;
  let fixture: ComponentFixture<FinanceCategoryModalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ FinanceCategoryModalComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(FinanceCategoryModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
