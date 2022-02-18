import { Pipe, PipeTransform } from '@angular/core';
import {isBoolean} from "lodash-es";

@Pipe({
  name: 'bool'
})
export class BoolPipe implements PipeTransform {
  transform(value: unknown, ...args: unknown[]): unknown {
    if (isBoolean(value)) {
      return value ? 'Yes' : 'No'
    }
    return 'Not provided'
  }
}
