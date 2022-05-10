import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { TemplateInternalBlockComponent } from './template-internal-block.component';

describe('TemplateInternalBlockComponent', () => {
  let component: TemplateInternalBlockComponent;
  let fixture: ComponentFixture<TemplateInternalBlockComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ TemplateInternalBlockComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TemplateInternalBlockComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
