import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TemplateBlockDetailsComponent } from './template-block-details.component';

describe('TemplateBlockDetailsComponent', () => {
  let component: TemplateBlockDetailsComponent;
  let fixture: ComponentFixture<TemplateBlockDetailsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ TemplateBlockDetailsComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(TemplateBlockDetailsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
