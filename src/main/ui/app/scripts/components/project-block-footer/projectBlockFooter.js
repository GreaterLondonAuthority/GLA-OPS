/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */


class ProjectBlockFooterCtrl {
  constructor(UserService) {
  }
}

ProjectBlockFooterCtrl.$inject = [];


angular.module('GLA')
  .component('projectBlockFooter', {
    templateUrl: 'scripts/components/project-block-footer/projectBlockFooter.html',
    bindings: {
      editableBlock: '<?'
    },
    controller: ProjectBlockFooterCtrl
  });
