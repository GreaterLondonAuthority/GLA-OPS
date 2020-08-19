import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class UserService {

  private user: any;

  constructor() {

  }

  currentUser() {
    let userStr = localStorage.getItem('ngStorage-user');
    return JSON.parse(userStr);
    // return this.localStorage.retrieve('ngStorage-user');
  }
}
