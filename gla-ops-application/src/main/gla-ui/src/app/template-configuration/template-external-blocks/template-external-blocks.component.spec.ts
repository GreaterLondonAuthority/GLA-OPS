import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TemplateExternalBlocksComponent } from './template-external-blocks.component';

describe('TemplateExternalBlocksComponent', () => {
  let component: TemplateExternalBlocksComponent;
  let fixture: ComponentFixture<TemplateExternalBlocksComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ TemplateExternalBlocksComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(TemplateExternalBlocksComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
