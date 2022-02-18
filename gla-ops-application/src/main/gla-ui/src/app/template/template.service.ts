import {Injectable} from '@angular/core';
import {find} from "lodash-es";
import {HttpClient} from "@angular/common/http";
import {environment} from "../../environments/environment";
import {Cacheable} from "ngx-cacheable";
import {Subject} from "rxjs";

const cacheBuster$ = new Subject<void>();

@Injectable({
  providedIn: 'root'
})
export class TemplateService {

  constructor(private http: HttpClient) {
  }

  /**
   * Retrieve all available templates
   */
  getAllProjectTemplates() {
    return this.http.get(`${environment.basePath}/templates`, {
      params: {}
    });
  }

  /**
   * Retrieve all available templates summaries
   */
  getAllProjectTemplatesSummaries() {
    return this.http.get(`${environment.basePath}/templatesSummaries`, {
      params: {}
    });
  }

  /**
   * Retrieve all available templates summaries by page
   */
  getAllProjectTemplateSummaries(page, programmeText, templateText, selectedTemplateStatuses) {
    let params = {
      programmeText: programmeText,
      templateText: templateText,
      selectedTemplateStatuses: selectedTemplateStatuses,
      page: page,
      size: '50',
      sort: 'id,asc'
    }
    return this.http.get(`${environment.basePath}/templates/summary`, {params})
  }


  /**
   * Retrieve template by id
   * @param {Number} id - template id
   * @return {Object} promise
   */
  @Cacheable({
    maxAge: 300*1000,
    maxCacheCount: 20,
    cacheBusterObserver: cacheBuster$.asObservable()
  })
  getTemplate(id, sanitise, cache) {
    if (!cache) {
      this.clearCache();
    }

    return this.http.get(`${environment.basePath}/templates/${id}`, {
      params: {
        sanitise: sanitise
      }
    });
  }

  /**
   * Retrieve draft template by id
   * @param {Number} id - template id
   * @return {Object} promise
   */
  getDraftTemplate(id) {
    return this.http.get(`${environment.basePath}/templates/${id}`, {
      params: {
        sanitise: 'false',
        jsonclob: 'true'
      }
    });
  }

  /**
   * create draft template
   */
  createTemplate(jsonString) {
    return this.http.post(`${environment.basePath}/templates/draft`, jsonString);
  }


  /**
   * validate template
   */
  validateTemplate(jsonString) {
    return this.http.post(`${environment.basePath}/templates/validate`, jsonString);
  }

  /**
   * updates template without projects
   */
  updateTemplate(id, jsonString) {
    return this.http.put(`${environment.basePath}/templates/${id}`, jsonString);
  }

  /**
   * updates template which has existing projects, should only be used for safe fields
   */
  updateInUseTemplate(id, jsonString) {
    return this.http.put(`${environment.basePath}/templates/inUse/${id}`, jsonString);
  }

  /**
   * perform the given command
   */
  performCommand(id, internalBlock, displayOrder, command, payload) {
    return this.http.put(`${environment.basePath}/templates/${id}/displayOrder/${displayOrder}/performCommand?command=${command}&internalBlock=${internalBlock}`, payload);
  }

  /**
   * Retrieve available deliverable types
   * @param {Number} id - template id
   * @return {Object} promise
   */
  getAvailableDeliverableTypes(id) {
    return this.http.get(`${environment.basePath}/templates/${id}/deliverableTypes`);
  }

  getBlockConfig(template, block) {
    return find(template.blocksEnabled, {block: block.blockType});
  }

  clearCache(){
    cacheBuster$.next();
  }
}
