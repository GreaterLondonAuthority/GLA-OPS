import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { InternalProjectAdminBlockComponent } from './internal-project-admin-block.component';

describe('InternalProjectAdminBlockComponent', () => {
  let component: InternalProjectAdminBlockComponent;
  let fixture: ComponentFixture<InternalProjectAdminBlockComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ InternalProjectAdminBlockComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(InternalProjectAdminBlockComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
