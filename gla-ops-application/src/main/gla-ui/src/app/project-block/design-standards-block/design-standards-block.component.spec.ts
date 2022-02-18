import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import {DesignStandardsBlockComponent} from './design-standards-block.component';

describe('DesignStandardsComponent', () => {
  let component: DesignStandardsBlockComponent;
  let fixture: ComponentFixture<DesignStandardsBlockComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ DesignStandardsBlockComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DesignStandardsBlockComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
