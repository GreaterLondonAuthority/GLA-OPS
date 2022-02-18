import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { TemplateBlockGrantSourceComponent } from './template-block-grant-source.component';

describe('TemplateBlockGrantSourceComponent', () => {
  let component: TemplateBlockGrantSourceComponent;
  let fixture: ComponentFixture<TemplateBlockGrantSourceComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ TemplateBlockGrantSourceComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TemplateBlockGrantSourceComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
