import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import {TemplateBlockDeliveryPartnersComponent} from './template-block-delivery-partners.component';

describe('TemplateBlockDeliveryPartnersComponent', () => {
  let component: TemplateBlockDeliveryPartnersComponent;
  let fixture: ComponentFixture<TemplateBlockDeliveryPartnersComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ TemplateBlockDeliveryPartnersComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TemplateBlockDeliveryPartnersComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
