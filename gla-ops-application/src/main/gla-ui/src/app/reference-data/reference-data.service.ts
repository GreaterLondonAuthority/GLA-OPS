import {Injectable} from '@angular/core';
import {environment} from "../../environments/environment";
import {HttpClient} from "@angular/common/http";
import {Cacheable} from 'ngx-cacheable';


@Injectable({
  providedIn: 'root'
})
export class ReferenceDataService {

  constructor(private http: HttpClient) {
  }

  @Cacheable()
  getBoroughs() {
    return this.http.get(`${environment.basePath}/boroughs`);
  }

  getConfigItemsByExternalId(externalId) {
    return this.http.get(`${environment.basePath}/configItems/${externalId}`);
  }

  @Cacheable()
  getAvailablePaymentSources() {
    return this.http.get(`${environment.basePath}/paymentSources`);
  }

  getBlockTypes() {
    return this.http.get(`${environment.basePath}/templates/projectBlockTypes`);
  }

  @Cacheable()
  getIcons() {
    return this.http.get(`${environment.basePath}/files`, {
      params: {category: 'Icon'}
    });
  }

  getRequirementOptions(): { id: string; label: string }[] {
    return [{
      label: 'Hidden',
      id: 'hidden'
    }, {
      label: 'Mandatory',
      id: 'mandatory'
    }, {
      label: 'Optional',
      id: 'optional'
    }]
  }
}
