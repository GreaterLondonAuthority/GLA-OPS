import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {YesNoInputComponent} from './yes-no-input.component';

describe('YesNoInputComponent', () => {
  let component: YesNoInputComponent;
  let fixture: ComponentFixture<YesNoInputComponent>;

  beforeEach(async(() => {
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
