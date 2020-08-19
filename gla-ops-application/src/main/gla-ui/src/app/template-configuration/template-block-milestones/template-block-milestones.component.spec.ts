import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {TemplateBlockMilestonesComponent} from './template-block-milestones.component';

describe('TemplateBlockMilestonesComponent', () => {
  let component: TemplateBlockMilestonesComponent;
  let fixture: ComponentFixture<TemplateBlockMilestonesComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ TemplateBlockMilestonesComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TemplateBlockMilestonesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
