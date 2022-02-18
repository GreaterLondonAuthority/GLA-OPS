import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import {ToggleIconComponent} from './toggle-icon.component';

describe('ToggleIconComponent', () => {
  let component: ToggleIconComponent;
  let fixture: ComponentFixture<ToggleIconComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ ToggleIconComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ToggleIconComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
