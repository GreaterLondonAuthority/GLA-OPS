import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import {BroadcastsComponent} from './broadcasts.component';

describe('BroadcastsComponent', () => {
  let component: BroadcastsComponent;
  let fixture: ComponentFixture<BroadcastsComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ BroadcastsComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(BroadcastsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
