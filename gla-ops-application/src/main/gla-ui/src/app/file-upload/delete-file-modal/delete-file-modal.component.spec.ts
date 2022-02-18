import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DeleteFileModalComponent } from './delete-file-modal.component';

describe('DeleteFileModalComponent', () => {
  let component: DeleteFileModalComponent;
  let fixture: ComponentFixture<DeleteFileModalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ DeleteFileModalComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(DeleteFileModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
