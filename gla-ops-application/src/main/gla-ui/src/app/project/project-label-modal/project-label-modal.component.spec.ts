import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ProjectLabelModalComponent } from './project-label-modal.component';

describe('ProjectLabelModalComponent', () => {
  let component: ProjectLabelModalComponent;
  let fixture: ComponentFixture<ProjectLabelModalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ProjectLabelModalComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ProjectLabelModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
