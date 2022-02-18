import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'fYear'
})
export class FYearPipe implements PipeTransform {

  transform(year: any, ...args: unknown[]): unknown {
    return year + '/' + (++year).toString().substr(2,2)
  }

}
