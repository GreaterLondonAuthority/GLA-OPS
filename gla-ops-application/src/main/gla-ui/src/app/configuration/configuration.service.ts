import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {environment} from "../../environments/environment";
import {catchError, map} from "rxjs/operators";
import {throwError} from "rxjs";
import {Cacheable} from "ngx-cacheable";

@Injectable({
  providedIn: 'root'
})
export class ConfigurationService {

  constructor(private http: HttpClient) {
  }

  /**
   * Loads app configuration
   * @returns {*}
   */
  @Cacheable()
  getConfig() {
    return this.http.get(`${environment.basePath}/config`);
  }

  /**
   * Returns all messages configured by admin
   * @returns {*}
   */
  getMessages() {
    return this.http.get(`${environment.basePath}/messages`);
  }


  comingSoonMessage() {
    // There is an issue that we are getting cached 'System is offline' page with status 200 from this api call.
    // To avoid api caching a unique requestTime is added as request parameter.
    // Also additional checks on the content are added to avoid injecting html page into home screen assuming its valid data
    return this.http.get(`${environment.basePath}/messages/coming-soon?requestTime=${new Date().getTime()}`, {responseType: 'text'})
      .pipe(
        map(response => {
          let restrictedWords = ['<html', '<body'];
          let containsRestricted = restrictedWords.some(phrase => (response || '').toLowerCase().indexOf(phrase) > -1);
          return containsRestricted ? null : response;
        }),
        catchError(error => {
          return throwError(error);
        })
      );
  }

  /**
   * Returns 'system outage' message configured by admin
   * @returns {*}
   */
  systemOutageMessage() {
    return this.getMessage('system-outage')
  }

  /**
   * Returns 'home page' message configured by admin
   * @returns {*}
   */
  homePageMessage() {
    return this.getMessage('home-page')
  }

  /**
   * Returns message configured by admin
   * @returns {*}
   */
  getMessage(messageKey) {
    return this.http.get(`${environment.basePath}/messages/${messageKey}`, {responseType: 'text'});
  }

  /**
   * Updates 'Coming Soon' message configured by admin
   * @param message <{code, text}>
   * @returns {*}
   */
  updateConfigMessage(message) {
    return this.http.put(`${environment.basePath}/messages/${message.code}`, message);
  }

  isResizeCssPropertySupported() {
    let textarea = document.createElement('textarea');
    return textarea.style.resize != undefined;
  }
}
