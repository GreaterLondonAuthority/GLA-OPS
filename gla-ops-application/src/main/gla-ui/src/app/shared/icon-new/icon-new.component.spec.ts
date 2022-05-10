import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { IconNewComponent } from './icon-new.component';

describe('IconNewComponent', () => {
  let component: IconNewComponent;
  let fixture: ComponentFixture<IconNewComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ IconNewComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(IconNewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
