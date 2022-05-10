import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ParentQuestionModalComponent } from './parent-question-modal.component';

describe('ParentQuestionModalComponent', () => {
  let component: ParentQuestionModalComponent;
  let fixture: ComponentFixture<ParentQuestionModalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ParentQuestionModalComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ParentQuestionModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
