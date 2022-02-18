import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TemplateBlockProjectElementsComponent } from './template-block-project-elements.component';

describe('TemplateBlockProjectElementsComponent', () => {
  let component: TemplateBlockProjectElementsComponent;
  let fixture: ComponentFixture<TemplateBlockProjectElementsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ TemplateBlockProjectElementsComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(TemplateBlockProjectElementsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
