/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

const gla = angular.module('GLA');

class CommentsList {
  save(){
    this.onSave({$event: {data: this.comment}});
    this.clear();
  }

  clear(){
    this.comment = null;
  }
}

gla.component('commentsForm', {
  templateUrl: 'scripts/components/comments-form/commentsForm.html',
  controller: CommentsList,
  bindings: {
    comment: '<?',
    onSave: '&'
  },
});

