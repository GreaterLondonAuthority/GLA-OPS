/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

const gla = angular.module('GLA');

class CommentsList {

}

gla.component('commentsList', {
  templateUrl: 'scripts/components/comments-list/commentsList.html',
  controller: CommentsList,
  bindings: {
    comments: '<comments',
    isOpen: '<?',
  },
});

