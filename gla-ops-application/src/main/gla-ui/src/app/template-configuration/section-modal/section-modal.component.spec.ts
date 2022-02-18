import {ComponentFixture, TestBed} from '@angular/core/testing';

import {SectionModalComponent} from './section-modal.component';

describe('SectionModalComponent', () => {
  let component: SectionModalComponent;
  let fixture: ComponentFixture<SectionModalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ SectionModalComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(SectionModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
