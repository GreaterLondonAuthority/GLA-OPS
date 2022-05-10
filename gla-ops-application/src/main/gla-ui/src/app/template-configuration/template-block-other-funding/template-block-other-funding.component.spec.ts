import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import {TemplateBlockOtherFundingComponent} from './template-block-other-funding.component';

describe('TemplateBlockOtherFundingComponent', () => {
  let component: TemplateBlockOtherFundingComponent;
  let fixture: ComponentFixture<TemplateBlockOtherFundingComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ TemplateBlockOtherFundingComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TemplateBlockOtherFundingComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
