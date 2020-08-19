import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {ProcessingRouteModalComponent} from './processing-route-modal.component';

describe('ProcessingRouteModalComponent', () => {
  let component: ProcessingRouteModalComponent;
  let fixture: ComponentFixture<ProcessingRouteModalComponent>;

  beforeEach(async(() => {
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
