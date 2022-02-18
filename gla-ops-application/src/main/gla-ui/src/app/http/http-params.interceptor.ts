import { Injectable } from '@angular/core';
import {
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpInterceptor
} from '@angular/common/http';
import { Observable } from 'rxjs';

/**
 * Removes param=undefined and param=null from url
 */
@Injectable()
export class HttpParamsInterceptor implements HttpInterceptor {

  constructor() {}

  public intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    let params = request.params;
    let internalParams = ['ignore403'];
    internalParams.forEach(param => {
      params = params.delete(param);
    })

    let paramValuesToRemove = [undefined, null];
    for (const key of request.params.keys()) {
      paramValuesToRemove.forEach(valueToRemove => {
        if (params.get(key) === valueToRemove) {
          // console.log(`removing ${key}=${valueToRemove} from http params for ${request.urlWithParams}`);
          params = params.delete(key, valueToRemove);
        }
      })
    }

    request = request.clone({ params });
    return next.handle(request);
  }
}
