import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ProjectShareModalComponent } from './project-share-modal.component';

describe('ProjectShareModalComponent', () => {
  let component: ProjectShareModalComponent;
  let fixture: ComponentFixture<ProjectShareModalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ProjectShareModalComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ProjectShareModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
