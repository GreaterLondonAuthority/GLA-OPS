import { Component, OnInit } from '@angular/core';
import {LocalStorageService} from "ngx-webstorage";
import {ConfigurationService} from "../configuration/configuration.service";

const COOKIE_WARNING_SESSION_ID = 'isCookieAccepted';

@Component({
  selector: 'gla-cookie-warning',
  templateUrl: './cookie-warning.component.html',
  styleUrls: ['./cookie-warning.component.scss']
})
export class CookieWarningComponent implements OnInit {

  constructor(private localStorageService: LocalStorageService, private configurationService: ConfigurationService) { }

  isCookieAccepted = false;

  privacyUrl = '';

  ngOnInit(): void {
    this.isCookieAccepted = !!(this.localStorageService.retrieve(COOKIE_WARNING_SESSION_ID));
    if (!this.isCookieAccepted) {
      this.configurationService.getConfig().subscribe(config => {
        if (config) {
          this.privacyUrl = config['privacy-policy-url'];
        }
      });
    }
  }

  close() {
    this.localStorageService.store(COOKIE_WARNING_SESSION_ID, true);
    this.isCookieAccepted = true;
  };
}
