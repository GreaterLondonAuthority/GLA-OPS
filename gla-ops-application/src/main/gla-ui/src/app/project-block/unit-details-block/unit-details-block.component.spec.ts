import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UnitDetailsBlockComponent } from './unit-details-block.component';

describe('UnitDetailsBlockComponent', () => {
  let component: UnitDetailsBlockComponent;
  let fixture: ComponentFixture<UnitDetailsBlockComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ UnitDetailsBlockComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(UnitDetailsBlockComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
