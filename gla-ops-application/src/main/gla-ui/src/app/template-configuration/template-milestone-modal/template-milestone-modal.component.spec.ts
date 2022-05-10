import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import {TemplateMilestoneModalComponent} from './template-milestone-modal.component';

describe('TemplateMilestoneModalComponent', () => {
  let component: TemplateMilestoneModalComponent;
  let fixture: ComponentFixture<TemplateMilestoneModalComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ TemplateMilestoneModalComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TemplateMilestoneModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
