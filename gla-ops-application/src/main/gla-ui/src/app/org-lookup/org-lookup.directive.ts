import {Directive, EventEmitter, OnDestroy, OnInit, Output} from '@angular/core';
import {NgControl} from "@angular/forms";
import {Subscription} from "rxjs";
import {OrganisationService} from "../organisation/organisation.service";

@Directive({
  selector: '[glaOrgLookup]'
})
export class OrgLookupDirective implements OnInit, OnDestroy {

  @Output() ngModelChange: EventEmitter<any> = new EventEmitter()
  @Output() glaOrgLookup: EventEmitter<string> = new EventEmitter()

  private subscription: Subscription;

  constructor(private ngControl: NgControl,
              private organisationService: OrganisationService) {
  }

  ngOnInit() {
    const control = this.ngControl.control;
    this.subscription = control.valueChanges
      .subscribe(orgCode => {
        if(orgCode && orgCode.length > 2) {
          this.searchOrg();
        } else {
          this.glaOrgLookup.emit(null)
        }
      });
  }

  ngOnDestroy() {
    this.subscription.unsubscribe();
  }

  searchOrg() {
    let orgCode = this.ngControl.control.value;
    if (orgCode) {
      this.organisationService.lookupOrgNameByCode(orgCode)
        .subscribe((response) => {
          //Don't update if request is out of order
          if (orgCode === this.ngControl.control.value) {
            // if (!response || response.status != 200) {
            if (!response) {
              this.glaOrgLookup.emit(null)
            } else {
              this.glaOrgLookup.emit(response as string)
            }
          }
        }, (err) => {
          if (orgCode === this.ngControl.control.value) {
            this.glaOrgLookup.emit(null)
          }
        })
    } else {
      this.glaOrgLookup.emit(null)
    }
  }
}
