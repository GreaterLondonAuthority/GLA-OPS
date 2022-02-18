import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {cloneDeep, filter, find, map, orderBy, remove, some, sortBy} from "lodash-es";
import {QuestionService} from '../question.service';

@Component({
  selector: 'gla-parent-question-modal',
  templateUrl: './parent-question-modal.component.html',
  styleUrls: ['./parent-question-modal.component.scss']
})
export class ParentQuestionModalComponent implements OnInit {

  @Input() questions: any[]
  @Input() sections: any[]
  @Input() availableParentQuestions: any[]
  parentQuestion: any
  selectedParentQuestionId: any = 'No Parent Question'
  parentQuestionId: any
  parentAnswerToMatch: any
  dropdownParent: boolean
  yesNoParent: boolean
  answerOptions: any
  isParentQuestionLevelValid: boolean
  errorMessage: string;

  constructor(public activeModal: NgbActiveModal, private questionService: QuestionService) {
  }

  ngOnInit(): void {
    this.questions = this.questions || [];
    this.sections = this.sections || [];
    this.availableParentQuestions = filter(orderBy(this.availableParentQuestions || [], 'displayOrder'));
    this.isParentQuestionLevelValid = true;
  }

  selectedParentQuestion() {
    if (this.selectedParentQuestionId === 'No Parent Question') {
      this.parentQuestionId = null
      this.parentAnswerToMatch = null;
      this.isParentQuestionLevelValid = true;
      this.dropdownParent = false;
      this.yesNoParent = false;
    } else {
      this.parentAnswerToMatch = null;
      this.questionService.getQuestion(this.selectedParentQuestionId).subscribe(rsp  => {
        this.parentQuestion = rsp;
        this.parentQuestionId = this.parentQuestion.id;
        this.answerOptions = this.parentQuestion.answerOptions;
        this.dropdownParent = this.parentQuestion.answerType === 'Dropdown' && this.answerOptions.length != 0
        this.yesNoParent = this.parentQuestion.answerType === 'YesNo'

        this.isParentQuestionLevelValid = true;
        this.isParentQuestionLevelValid = this.validateParentQuestionLevel(this.parentQuestion, this.questions)
      }, error => {
        this.errorMessage = error.error.description;
      });
    }
  }

  validateParentQuestionLevel(inQuestion, questions){
    return this.questionService.getQuestionUpLevel(inQuestion.id, questions) + this.questionService.getQuestionDownLevel(inQuestion.id, questions) < 4
  }

  onMultiSelectChange(check, question){
    let sortedAnswers = sortBy(question.answerOptions, 'displayOrder');
    let answers = (sortedAnswers || []).filter(ao => !!ao.model);
    this.parentAnswerToMatch = answers.map(ao => ao.option).join(question.delimiter);
  }

  saveParentQuestion() {
    this.questions.forEach(question => {
      if (question.isSelected) {
        question.parentId = this.parentQuestionId;
        question.parentAnswerToMatch = this.parentAnswerToMatch;
      }
    });

    this.sections.forEach(section => {
      if (section.isSelected) {
        section.parentId = this.parentQuestionId;
        section.parentAnswerToMatch = this.parentAnswerToMatch;
      }
    });

    this.activeModal.close(this.questions);
  }

}
