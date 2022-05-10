import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import {IndicativeGrantRequestedTableComponent} from './indicative-grant-requested-table.component';

describe('IndicativeGrantRequestedTableComponent', () => {
  let component: IndicativeGrantRequestedTableComponent;
  let fixture: ComponentFixture<IndicativeGrantRequestedTableComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ IndicativeGrantRequestedTableComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(IndicativeGrantRequestedTableComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
