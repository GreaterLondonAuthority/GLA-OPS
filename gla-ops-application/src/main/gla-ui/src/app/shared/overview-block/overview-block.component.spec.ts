import { ComponentFixture, TestBed } from '@angular/core/testing';

import { OverviewBlockComponent } from './overview-block.component';

describe('OverviewBlockComponent', () => {
  let component: OverviewBlockComponent;
  let fixture: ComponentFixture<OverviewBlockComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ OverviewBlockComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(OverviewBlockComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
