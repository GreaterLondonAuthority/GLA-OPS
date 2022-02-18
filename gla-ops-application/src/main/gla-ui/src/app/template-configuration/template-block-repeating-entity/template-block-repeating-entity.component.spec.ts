import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TemplateBlockRepeatingEntityComponent } from './template-block-repeating-entity.component';

describe('TemplateBlockRepeatingEntityComponent', () => {
  let component: TemplateBlockRepeatingEntityComponent;
  let fixture: ComponentFixture<TemplateBlockRepeatingEntityComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ TemplateBlockRepeatingEntityComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(TemplateBlockRepeatingEntityComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
