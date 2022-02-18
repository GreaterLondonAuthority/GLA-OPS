import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { TemplateBlockLearningGrantComponent } from './template-block-learning-grant.component';

describe('TemplateBlockLearningGrantComponent', () => {
  let component: TemplateBlockLearningGrantComponent;
  let fixture: ComponentFixture<TemplateBlockLearningGrantComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ TemplateBlockLearningGrantComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TemplateBlockLearningGrantComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
