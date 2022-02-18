import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import {ProjectAssignModalComponent} from './project-assign-modal.component';

describe('ProjectAssignModalComponent', () => {
  let component: ProjectAssignModalComponent;
  let fixture: ComponentFixture<ProjectAssignModalComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ ProjectAssignModalComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ProjectAssignModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
