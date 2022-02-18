import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TemplateBlockUserDefinedOutputsComponent } from './template-block-user-defined-outputs.component';

describe('TemplateBlockUserDefinedOutputsComponent', () => {
  let component: TemplateBlockUserDefinedOutputsComponent;
  let fixture: ComponentFixture<TemplateBlockUserDefinedOutputsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ TemplateBlockUserDefinedOutputsComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(TemplateBlockUserDefinedOutputsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
