import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ProjectTransferModalComponent } from './project-transfer-modal.component';

describe('ProjectTransferModalComponent', () => {
  let component: ProjectTransferModalComponent;
  let fixture: ComponentFixture<ProjectTransferModalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ProjectTransferModalComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ProjectTransferModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
