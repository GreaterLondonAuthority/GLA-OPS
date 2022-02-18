import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { TemplateInternalBlocksComponent } from './template-internal-blocks.component';

describe('TemplateInternalBlocksComponent', () => {
  let component: TemplateInternalBlocksComponent;
  let fixture: ComponentFixture<TemplateInternalBlocksComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ TemplateInternalBlocksComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TemplateInternalBlocksComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
