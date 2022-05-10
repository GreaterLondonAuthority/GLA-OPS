import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import {OutputCategoriesComponent} from './output-categories.component';

describe('OutputCategoriesComponent', () => {
  let component: OutputCategoriesComponent;
  let fixture: ComponentFixture<OutputCategoriesComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ OutputCategoriesComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(OutputCategoriesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
