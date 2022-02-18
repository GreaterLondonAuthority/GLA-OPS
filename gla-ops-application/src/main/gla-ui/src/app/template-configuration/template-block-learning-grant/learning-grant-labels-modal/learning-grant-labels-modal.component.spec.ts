import { ComponentFixture, TestBed } from '@angular/core/testing';

import { LearningGrantLabelsModalComponent } from './learning-grant-labels-modal.component';

describe('LearningGrantLabelsModalComponent', () => {
  let component: LearningGrantLabelsModalComponent;
  let fixture: ComponentFixture<LearningGrantLabelsModalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ LearningGrantLabelsModalComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(LearningGrantLabelsModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
