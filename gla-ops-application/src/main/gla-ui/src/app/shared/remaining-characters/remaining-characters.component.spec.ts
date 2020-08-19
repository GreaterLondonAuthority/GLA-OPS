import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { RemainingCharactersComponent } from './remaining-characters.component';

describe('RemainingCharactersComponent', () => {
  let component: RemainingCharactersComponent;
  let fixture: ComponentFixture<RemainingCharactersComponent>;

  beforeEach(async(() => {
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
