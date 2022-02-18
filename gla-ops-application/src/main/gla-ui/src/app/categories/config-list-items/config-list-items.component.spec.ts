import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ConfigListItemsComponent } from './config-list-items.component';

describe('ConfigListItemsComponent', () => {
  let component: ConfigListItemsComponent;
  let fixture: ComponentFixture<ConfigListItemsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ConfigListItemsComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ConfigListItemsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
