import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TemplateBlockProjectObjectivesComponent } from './template-block-project-objectives.component';

describe('TemplateBlockProjectObjectivesComponent', () => {
  let component: TemplateBlockProjectObjectivesComponent;
  let fixture: ComponentFixture<TemplateBlockProjectObjectivesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ TemplateBlockProjectObjectivesComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(TemplateBlockProjectObjectivesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
