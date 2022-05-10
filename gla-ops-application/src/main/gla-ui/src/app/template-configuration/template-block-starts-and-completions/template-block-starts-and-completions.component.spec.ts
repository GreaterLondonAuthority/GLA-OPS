import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TemplateBlockStartsAndCompletionsComponent } from './template-block-starts-and-completions.component';

describe('TemplateBlockStartsAndCompletionsComponent', () => {
  let component: TemplateBlockStartsAndCompletionsComponent;
  let fixture: ComponentFixture<TemplateBlockStartsAndCompletionsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ TemplateBlockStartsAndCompletionsComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(TemplateBlockStartsAndCompletionsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
