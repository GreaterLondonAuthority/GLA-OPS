import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import {AffordableHomesBlockComponent} from './affordable-homes-block.component';

describe('IndicativeStartsAndCompletionsBlockComponent', () => {
  let component: AffordableHomesBlockComponent;
  let fixture: ComponentFixture<AffordableHomesBlockComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ AffordableHomesBlockComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AffordableHomesBlockComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
