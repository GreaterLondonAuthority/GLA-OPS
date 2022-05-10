import {ComponentFixture, TestBed} from '@angular/core/testing';

import {TemplateInternalBlockQuestionsCommentsComponent} from './template-internal-block-questions-comments.component';

describe('TemplateInternalBlockQuestionsCommentsComponent', () => {
  let component: TemplateInternalBlockQuestionsCommentsComponent;
  let fixture: ComponentFixture<TemplateInternalBlockQuestionsCommentsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ TemplateInternalBlockQuestionsCommentsComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(TemplateInternalBlockQuestionsCommentsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
