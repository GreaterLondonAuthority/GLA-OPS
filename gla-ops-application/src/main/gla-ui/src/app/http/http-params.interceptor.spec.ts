import { TestBed } from '@angular/core/testing';

import { HttpParamsInterceptor } from './http-params.interceptor';

describe('HttpParamsInterceptor', () => {
  beforeEach(() => TestBed.configureTestingModule({
    providers: [
      HttpParamsInterceptor
      ]
  }));

  it('should be created', () => {
    const interceptor: HttpParamsInterceptor = TestBed.inject(HttpParamsInterceptor);
    expect(interceptor).toBeTruthy();
  });
});
