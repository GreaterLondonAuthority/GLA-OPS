import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import {AffordableHomesTableComponent} from './affordable-homes-table.component';

describe('IndicativeStartsAndCompletionsTableComponent', () => {
  let component: AffordableHomesTableComponent;
  let fixture: ComponentFixture<AffordableHomesTableComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ AffordableHomesTableComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AffordableHomesTableComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
