import {Injectable} from '@angular/core';
import {environment} from "../../environments/environment";
import {HttpClient} from "@angular/common/http";

@Injectable({
  providedIn: 'root'
})
export class OrganisationService {

  constructor(private http: HttpClient) {
  }

  /**
   * Looks up an organisation name by ID or provider number, return a 404 if not found.
   * @param  {String} orgCode organisation ID or provider number
   * @return {Object} promise
   */
  lookupOrgNameByCode(orgCode) {
    let code = (orgCode || '').toString();
    return this.http.get(`${environment.basePath}/organisations/${code}/name`, {responseType: 'text'});
  }

  updateContractStatus(organisationId, contractId, contract) {
    if (contractId) {
      return this.http.put(`${environment.basePath}/organisations/${organisationId}/contracts/${contractId}`, contract);
    } else {
      return this.http.post(`${environment.basePath}/organisations/${organisationId}/contracts`, contract)
    }
  }

  getContracts()  {
    return this.http.get(`${environment.basePath}/contractsWithTemplates`);
  }

  deleteVariation(organisationId, contractId)  {
    return this.http.delete(`${environment.basePath}/organisations/${organisationId}/contracts/${contractId}`);
  }

  /**
   * External users should be able to accept the contract/variation offered to them by GLA using this.
   * @param  {Integer}  organisation id
   * @param  {Integer}  contract id
   * @param  {Contract}  contractModel fields
   * @return {Object} promise
   */
  acceptContract(organisationId, contractId, contract) {
    return this.http.put(`${environment.basePath}/organisations/${organisationId}/contracts/${contractId}/accept`, contract);
  }

}
