import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {BlockUsageComponent} from './block-usage.component';

describe('BlockUsageComponent', () => {
  let component: BlockUsageComponent;
  let fixture: ComponentFixture<BlockUsageComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ BlockUsageComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(BlockUsageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
