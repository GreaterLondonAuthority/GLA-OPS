import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {UserService} from "../user/user.service";
import {environment} from "../../environments/environment";
import {Subject} from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class MetadataService {

  private metadataSubject = new Subject<any>();

  constructor(private http: HttpClient, private userService: UserService) {
  }

  onMetadataChange(callback){
    return this.metadataSubject.subscribe(callback)
  }

  fireMetadataUpdate() {
    let user = this.userService.currentUser();
    if (user && user.loggedOn) {
      let params = {
        ignore403: 'true'
      };
      this.http.get(`${environment.basePath}/metadata/`, {params}).subscribe(rsp => {
        this.metadataSubject.next(rsp);
        return rsp;
      }, error => {
        console.error('Failed to get meta data, ', error)
      });
    } else {
      this.metadataSubject.next({loggedOut: true});
    }
  }
}
