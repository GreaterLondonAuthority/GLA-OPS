import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {isBoolean} from "lodash-es";

@Component({
  selector: 'gla-yes-no-input',
  templateUrl: './yes-no-input.component.html',
  styleUrls: ['./yes-no-input.component.scss']
})
export class YesNoInputComponent implements OnInit {

  @Input() name: string;
  @Input() mode: string;
  @Input() model: any;
  @Input () disabled: boolean;
  @Input () required: boolean;
  @Input () readOnly = false;
  @Output() modelChange = new EventEmitter();


  yesValue: boolean | string;
  noValue: boolean | string;

  constructor() { }

  ngOnInit(): void {
    this.yesValue = this.mode == 'bool' ? true : 'yes';
    this.noValue = this.mode == 'bool' ? false : 'no';
  }

  onValueChange(value){
    this.modelChange.emit(value);
  }

  getReadOnlyText(){
    let boolValue;
    if (this.model === 'yes' || this.model === true) {
      boolValue = true;
    } else if (this.model === 'no' || this.model === false) {
      boolValue = false;
    }

    if (isBoolean(boolValue)) {
      return boolValue ? 'Yes' : 'No'
    }
    return 'Not provided'
  }
}
