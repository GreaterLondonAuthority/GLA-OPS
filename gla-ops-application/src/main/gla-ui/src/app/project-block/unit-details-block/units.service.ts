import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class UnitsService {
  public LEGACY_RENT_MARKET_TYPE_ID = 2
  public DISCOUNTED_RATE_MARKET_TYPE_ID = 4
  public LEGACY_SALES_MARKET_TYPE_ID = 5

  constructor() {
  }
}
