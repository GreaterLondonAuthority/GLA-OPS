import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import {YesNoInputComponent} from './yes-no-input.component';

describe('YesNoInputComponent', () => {
  let component: YesNoInputComponent;
  let fixture: ComponentFixture<YesNoInputComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ YesNoInputComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(YesNoInputComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
