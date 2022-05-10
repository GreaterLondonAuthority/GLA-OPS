import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {environment} from "../../../environments/environment";


@Injectable({
  providedIn: 'root'
})
export class NotificationService {

  constructor(private http: HttpClient) { }

  getAllEmailsByRecipientAndSubjectAndBody(config : any){
    return this.http.get(`${environment.basePath}/emails`, config);
  }

  watchEntity(userName, entityId, entityType){
    return this.http.post(`${environment.basePath}/subscriptions`, {
      username: userName,
      entityType: entityType,
      entityId: entityId
    });
  }

  unwatchEntity(username, projectId, entityType) {
    return this.http.delete(`${environment.basePath}/subscriptions/${username}/${entityType}/${projectId}`);
  }

  watchProject(username, projectId) {
    return this.watchEntity(username, projectId, 'project');
  }

  unwatchProject(username, projectId) {
    return this.unwatchEntity(username, projectId, 'project');
  }

}
