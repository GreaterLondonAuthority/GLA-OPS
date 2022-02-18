import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import {ProcessingRouteModalComponent} from './processing-route-modal.component';

describe('ProcessingRouteModalComponent', () => {
  let component: ProcessingRouteModalComponent;
  let fixture: ComponentFixture<ProcessingRouteModalComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ ProcessingRouteModalComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ProcessingRouteModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
