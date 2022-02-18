import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FinanceCategoriesComponent } from './finance-categories.component';

describe('FinanceCategoriesComponent', () => {
  let component: FinanceCategoriesComponent;
  let fixture: ComponentFixture<FinanceCategoriesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ FinanceCategoriesComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(FinanceCategoriesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
