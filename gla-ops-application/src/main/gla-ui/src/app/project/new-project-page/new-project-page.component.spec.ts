import { ComponentFixture, TestBed } from '@angular/core/testing';

import { NewProjectPageComponent } from './new-project-page.component';

describe('NewProjectPageComponent', () => {
  let component: NewProjectPageComponent;
  let fixture: ComponentFixture<NewProjectPageComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ NewProjectPageComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(NewProjectPageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
