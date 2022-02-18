import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { TemplateBlockFundingComponent } from './template-block-funding.component';

describe('TemplateBlockFundingComponent', () => {
  let component: TemplateBlockFundingComponent;
  let fixture: ComponentFixture<TemplateBlockFundingComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ TemplateBlockFundingComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TemplateBlockFundingComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
