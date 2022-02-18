import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UserPasswordResetModalComponent } from './user-password-reset-modal.component';

describe('UserPasswordResetModalComponent', () => {
  let component: UserPasswordResetModalComponent;
  let fixture: ComponentFixture<UserPasswordResetModalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ UserPasswordResetModalComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(UserPasswordResetModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
