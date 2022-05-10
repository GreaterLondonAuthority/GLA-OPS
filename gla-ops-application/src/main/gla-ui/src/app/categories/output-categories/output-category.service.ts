import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {environment} from "../../../environments/environment";

@Injectable({
  providedIn: 'root'
})
export class OutputCategoryService {

  constructor(private http: HttpClient) { }

  createOutputConfigurationGroup(data){
    let suffix = data.groupId ? `?groupId=${data.groupId}` : '';
    return this.http.post(`${environment.basePath}/outputCategory` + suffix , data);
  }

  getAllOutputConfiguration(config : any){
    return this.http.get(`${environment.basePath}/outputCategory`, config);
  }
}
