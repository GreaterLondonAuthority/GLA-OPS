import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TemplateExternalBlockComponent } from './template-external-block.component';

describe('TemplateExternalBlockComponent', () => {
  let component: TemplateExternalBlockComponent;
  let fixture: ComponentFixture<TemplateExternalBlockComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ TemplateExternalBlockComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(TemplateExternalBlockComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
