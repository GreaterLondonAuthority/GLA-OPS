import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TemplateBlockQuestionsComponent } from './template-block-questions.component';

describe('TemplateBlockQuestionsComponent', () => {
  let component: TemplateBlockQuestionsComponent;
  let fixture: ComponentFixture<TemplateBlockQuestionsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ TemplateBlockQuestionsComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(TemplateBlockQuestionsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
