import {Injectable} from '@angular/core';
import {environment} from '../../environments/environment'
import {HttpClient} from "@angular/common/http";
import {FeatureToggle} from "./feature-toggle";

@Injectable({
  providedIn: 'root'
})
export class FeatureToggleService {

  constructor(private http: HttpClient) {
  }

  isFeatureEnabled(feature: string) {
    return this.http.get<boolean>(`${environment.basePath}/features/${feature}`);
  }

  getFeatures() {
    return this.http.get<FeatureToggle>(`${environment.basePath}/features`);
  }

  updateFeature(feature: string, enabled: boolean) {
    return this.http.post<FeatureToggle>(`${environment.basePath}/features/${feature}`, enabled);
  }
}
