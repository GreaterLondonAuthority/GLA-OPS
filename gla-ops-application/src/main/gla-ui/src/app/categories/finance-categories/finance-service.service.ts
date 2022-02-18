import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {environment} from "../../../environments/environment";

import {map} from "rxjs/operators";
import {filter} from "lodash-es";

@Injectable({
  providedIn: 'root'
})
export class FinanceService {

  constructor(private http: HttpClient) { }

  getFinanceCategories(reload : any, config : any){
    // TODO: Add cache back {cache: !reload}
    return this.http.get(`${environment.basePath}/finance/categories`, config);
  }

  getReceiptCategories(reload : any): any {
    return this.getFinanceCategories(reload, {}).pipe(
      map(rsp => {
         return filter(rsp, {receiptStatus: 'ReadWrite'});
      })
    )
  }


  getSpendCategories(reload : any){
    return this.getFinanceCategories(reload, {}).pipe(
      map(rsp => {
        return filter(rsp, {spendStatus: 'ReadWrite'});
      })
    )
  }

  updateCategory(data){
    let id = data.id;
    return this.http.put(`${environment.basePath}/finance/categories/${id}`, data);
  }

  createCategory(data){
    return this.http.post(`${environment.basePath}/finance/categories/`, data);
  }
}
