import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { RemainingCharactersComponent } from './remaining-characters.component';

describe('RemainingCharactersComponent', () => {
  let component: RemainingCharactersComponent;
  let fixture: ComponentFixture<RemainingCharactersComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ RemainingCharactersComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RemainingCharactersComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
