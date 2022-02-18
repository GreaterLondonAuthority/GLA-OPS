import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {environment} from "../../environments/environment";

@Injectable({
  providedIn: 'root'
})
export class PostLoginService {

  constructor(private http: HttpClient) {
  }

  checkForDeprecatedOrganisationsType() {
    return this.http.get(`${environment.basePath}/postLogin/checkOrgType`, {responseType: 'text'});
  }
}
