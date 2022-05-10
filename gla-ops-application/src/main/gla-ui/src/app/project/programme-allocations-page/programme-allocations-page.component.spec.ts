import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ProgrammeAllocationsPageComponent } from './programme-allocations-page.component';

describe('ProgrammeAllocationsPageComponent', () => {
  let component: ProgrammeAllocationsPageComponent;
  let fixture: ComponentFixture<ProgrammeAllocationsPageComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ProgrammeAllocationsPageComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ProgrammeAllocationsPageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
