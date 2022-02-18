import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { VersionHistoryModalComponent } from './version-history-modal.component';

describe('VersionHistoryModalComponent', () => {
  let component: VersionHistoryModalComponent;
  let fixture: ComponentFixture<VersionHistoryModalComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ VersionHistoryModalComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(VersionHistoryModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
