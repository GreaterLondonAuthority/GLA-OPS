/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

const gla = angular.module('GLA');

class ProgrammesCarousel {

  constructor(orderByFilter) {
    this.orderByFilter = orderByFilter;
  }

  $onInit() {
    this.index = 0;
    this.programmesGroup.programmes = this.orderByFilter(this.programmesGroup.programmes, '-lastPublicEdit');
    if(this.programmesGroup.programmes.length) {
      this.selectedProgramme = this.programmesGroup.programmes[this.index];
    }

    console.log('this.selectedProgramme', this.selectedProgramme)
  }

  previous() {
    if (this.index == 0) {
      this.index = this.programmesGroup.programmes.length - 1;
    } else {

      this.index--;
    }
    this.selectedProgramme = this.programmesGroup.programmes[this.index];
  }

  next() {
    if (this.index == this.programmesGroup.programmes.length - 1) {
      this.index = 0;
    } else {
      this.index++;
    }
    this.selectedProgramme = this.programmesGroup.programmes[this.index];
  }
}

ProgrammesCarousel.$inject = ['orderByFilter'];


gla.component('programmesCarousel', {
  templateUrl: 'scripts/components/programmes-carousel/programmesCarousel.html',
  controller: ProgrammesCarousel,
  bindings: {
    programmesGroup: '<',
    onRegister: '&'
  },
});

