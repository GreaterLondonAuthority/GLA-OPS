import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {environment} from "../../../environments/environment";

@Injectable({
  providedIn: 'root'
})
export class OutputsConfigurationService {

  constructor(private http: HttpClient) {
  }

  getAllOutputConfiguration(){
    return this.http.get(`${environment.basePath}/outputCategory`);
  }
  getAllOutputConfigurationGroup(){
    return this.http.get(`${environment.basePath}/outputConfigurationGroup/`);
  }
  createOutputConfigurationGroup(data){
    return this.http.post(`${environment.basePath}/outputConfigurationGroup/`, data);
  }

}
