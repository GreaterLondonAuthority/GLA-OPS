import {Directive, EventEmitter, HostListener, OnDestroy, OnInit, Output} from '@angular/core';
import {NgControl} from "@angular/forms";
import {Subscription} from "rxjs";
import {isNaN, isNumber} from "lodash-es";

@Directive({
  selector: '[numberType]'
})
export class NumberTypeDirective implements OnInit, OnDestroy {

  @Output() ngModelChange: EventEmitter<any> = new EventEmitter()

  private subscription: Subscription;

  constructor(private ngControl: NgControl) {
  }

  ngOnInit() {
    const control = this.ngControl.control;
    this.subscription = control.valueChanges
      .subscribe(v => {
        if (this.isModelUpdateRequired(v)) {
          this.updateModel(v);
        }
      });
  }

  ngOnDestroy() {
    this.subscription.unsubscribe();
  }

  /**
   * There is some strange behaviour in ngx-mask that when you start typing '-' or '0'
   * into a number field, it sets model to 'NaN' or '0' and doesn't work properly if changed to
   * null or 0 (integer). As a workaround for this case we keep the original values set by ngx-mask and
   * update the model to correct type on blur instead
   * @param modelValue
   */
  isModelUpdateRequired(modelValue) {
    return !(typeof modelValue === 'string' && modelValue !== '' && (modelValue === 'NaN' || +modelValue === 0));
  }

  updateModel(existingValue) {
    const control = this.ngControl.control;
    let newValue;
    if (existingValue === 0) {
      newValue = '0';
    } else if (isNaN(existingValue)) {
      newValue = 'NaN';
    } else {
      newValue = this.toNumber(existingValue);
    }
    control.setValue(newValue, {emitEvent: false, emitModelToViewChange: false})
  }

  toNumber(value) {
    if (value == null || isNumber(value)) {
      return value;
    } else if (value === '' || value === '-' || isNaN(+value)) {
      return null
    }
    return +value;
  }

  @HostListener('focus')
  public onFocus(): void {
    // console.log('focus', this.ngControl.value, this.toNumber(this.ngControl.value));
    if (this.ngControl.value === 0 || this.ngControl.value === '0') {
      this.ngControl.control.setValue('0', {emitEvent: false, emitModelToViewChange: true})
    }
  }

  @HostListener('blur')
  public onBlur(): void {
    // console.log('blur', this.ngControl.value, this.toNumber(this.ngControl.value));
    this.ngControl.control.setValue(this.toNumber(this.ngControl.value), {
      emitEvent: false,
      emitModelToViewChange: true
    })
  }
}
