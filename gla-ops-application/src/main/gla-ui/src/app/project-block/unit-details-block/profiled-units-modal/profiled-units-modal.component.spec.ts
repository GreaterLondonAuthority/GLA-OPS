import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ProfiledUnitsModalComponent } from './profiled-units-modal.component';

describe('ProfiledUnitsModalComponent', () => {
  let component: ProfiledUnitsModalComponent;
  let fixture: ComponentFixture<ProfiledUnitsModalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ProfiledUnitsModalComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ProfiledUnitsModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
