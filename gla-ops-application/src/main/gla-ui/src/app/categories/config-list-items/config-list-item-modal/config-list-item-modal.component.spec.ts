import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ConfigListItemModalComponent } from './config-list-item-modal.component';

describe('ConfigListItemModalComponent', () => {
  let component: ConfigListItemModalComponent;
  let fixture: ComponentFixture<ConfigListItemModalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ConfigListItemModalComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ConfigListItemModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
