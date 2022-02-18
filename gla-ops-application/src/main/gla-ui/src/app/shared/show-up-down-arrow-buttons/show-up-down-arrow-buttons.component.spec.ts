import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import {ShowUpDownArrowButtonsComponent} from './show-up-down-arrow-buttons.component';

describe('ShowUpDownArrowButtonsComponent', () => {
  let component: ShowUpDownArrowButtonsComponent;
  let fixture: ComponentFixture<ShowUpDownArrowButtonsComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ ShowUpDownArrowButtonsComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ShowUpDownArrowButtonsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
