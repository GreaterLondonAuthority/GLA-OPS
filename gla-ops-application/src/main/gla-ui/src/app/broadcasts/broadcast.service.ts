import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {environment} from "../../environments/environment";

@Injectable({
  providedIn: 'root'
})
export class BroadcastService {

  constructor(private http: HttpClient) { }

  getBroadcasts() {
    return this.http.get(`${environment.basePath}/broadcasts`);
  }

  getBroadcast(id: number){
    return this.http.get(`${environment.basePath}/broadcasts/${id}`);
  }

  createBroadcast(broadcast: any){
    return this.http.post(`${environment.basePath}/broadcasts`, broadcast);
  }

  approveBroadcast(id: number){
    return this.http.put(`${environment.basePath}/approveBroadcast/${id}`, 'approve');
  }

  deleteBroadcast(id: number){
    console.log('deleting');
    return this.http.delete(`${environment.basePath}/broadcasts/${id}`);
  }

}
