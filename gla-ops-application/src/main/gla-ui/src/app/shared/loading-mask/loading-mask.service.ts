import { Injectable } from '@angular/core';
import {Observer, Subject} from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class LoadingMaskService {

  loadingMaskVisible = false;

  private loadingMaskVisibilityChange: Subject<boolean> = new Subject<boolean>();

  constructor()  {
    this.loadingMaskVisibilityChange.subscribe((value) => {
      this.loadingMaskVisible = value
    });
  }

  showLoadingMask(isVisible: boolean){
    this.loadingMaskVisibilityChange.next(isVisible);
  }

  subscribe(callback: Observer<boolean>){
    return this.loadingMaskVisibilityChange.subscribe(callback)
  }
}
