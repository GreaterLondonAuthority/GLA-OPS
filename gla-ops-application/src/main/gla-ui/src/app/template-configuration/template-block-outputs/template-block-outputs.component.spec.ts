import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TemplateBlockOutputsComponent } from './template-block-outputs.component';

describe('TemplateBlockOutputsComponent', () => {
  let component: TemplateBlockOutputsComponent;
  let fixture: ComponentFixture<TemplateBlockOutputsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ TemplateBlockOutputsComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(TemplateBlockOutputsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
