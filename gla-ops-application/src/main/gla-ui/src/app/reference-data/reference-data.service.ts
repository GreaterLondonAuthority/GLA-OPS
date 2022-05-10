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

  getConfigItems() {
    return this.http.get(`${environment.basePath}/configItems`);
  }

  getConfigItemsByType(type, config){
    return this.http.get(`${environment.basePath}/configItemsGroups?type=${type}`, config);
  }

  updateConfigItem(item){
    return this.http.put(`${environment.basePath}/configItems/${item.id}`, item );
  }

  deleteConfigItem(externalId, categoryId){
    return this.http.delete(`${environment.basePath}/configItems/${externalId}/${categoryId}`);
  }

  deleteConfigGroup(externalId){
    return this.http.delete(`${environment.basePath}/configItems/${externalId}`);
  }

  createConfigItem(item){
    return this.http.post(`${environment.basePath}/configItems`, new Array(item));
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

  getInternalBlockTypes() {
    return this.http.get(`${environment.basePath}/templates/projectInternalBlockTypes`);
  }

  getBlockUsage(projectBlockType, internalBlockType) {
    return this.http.get(`${environment.basePath}/blockUsage?projectBlockType=${projectBlockType}&internalBlockType=${internalBlockType}`);
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

  getFundingSpendTypeOptions(): { id: string; label: string }[] {
    return [{
      label: 'Capital Only',
      id: 'CAPITAL_ONLY'
    }, {
      label: 'Revenue Only',
      id: 'REVENUE_ONLY'
    }, {
      label: 'Revenue and Capital',
      id: 'REVENUE_AND_CAPITAL'
    }]
  }

  getAllocationProfileOptions(): { id: string; label: string}[] {
    return [{
      id: 'AEB_PROCURED',
      label: 'AEB PROCURED PROFILE'
    }, {
      id: 'AEB_GRANT',
      label: 'AEB GRANT PROFILE'
    }]
  }

  getAllocationTypesOptions(): { id: string; label: string}[] {
    return [{
      id: 'Delivery',
      label: 'Delivery'
    }, {
      id: 'LearnerSupport',
      label: 'Learner Support'
    }, {
      id: 'Community',
      label: 'Community'
    }, {
      id: 'InnovationFund',
      label: 'Innovation Fund'
    }, {
      id: 'ResponseFundStrand1',
      label: 'Response Fund Strand 1'
    }, {
      id: 'NationalSkillsFund',
      label: 'National Skills Fund'
    }]
  }
}
