import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ProjectOverviewBlockComponent } from './project-overview-block.component';

describe('ProjectOverviewBlockComponent', () => {
  let component: ProjectOverviewBlockComponent;
  let fixture: ComponentFixture<ProjectOverviewBlockComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ProjectOverviewBlockComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ProjectOverviewBlockComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
