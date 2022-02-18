import {Injectable} from '@angular/core';
import {environment} from "../../environments/environment";
import {HttpClient} from "@angular/common/http";

@Injectable({
  providedIn: 'root'
})
export class ProjectBlockService {

  constructor(private http: HttpClient) {
  }

  updateInternalBlock(projectId: number, block: any) {
    return this.http.put(`${environment.basePath}/projects/${projectId}/internalBlocks/${block.id}`, block);
  }

  updateBlock(projectId:number, blockId: number, block: any, releaseLock: any) {
    return this.http.put(`${environment.basePath}/projects/${projectId}/blocks/${blockId}?releaseLock=${!!releaseLock}`, block, releaseLock)
  }

  deleteBlock(projectId, blockId){
    return this.http.delete(`${environment.basePath}/projects/${projectId}/blocks/${blockId}`);
  }

  revertBlock(projectId, blockId){
    return this.http.put(`${environment.basePath}/projects/${projectId}/block/${blockId}/revert`, null);
  }
}
