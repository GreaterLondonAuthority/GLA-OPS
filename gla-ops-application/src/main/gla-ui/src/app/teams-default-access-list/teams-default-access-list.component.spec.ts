import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {TeamsDefaultAccessListComponent} from './teams-default-access-list.component';

describe('TeamsDefaultAccessListComponent', () => {
  let component: TeamsDefaultAccessListComponent;
  let fixture: ComponentFixture<TeamsDefaultAccessListComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ TeamsDefaultAccessListComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TeamsDefaultAccessListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
