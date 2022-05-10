import {Component, Input, OnInit} from '@angular/core';
import {cloneDeep, find, some} from "lodash-es";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {QuestionService} from "../template-block-questions/question.service";

@Component({
  selector: 'gla-question-modal',
  templateUrl: './question-modal.component.html',
  styleUrls: ['./question-modal.component.scss']
})
export class QuestionModalComponent implements OnInit {

  @Input() question: any
  @Input() questions: any
  @Input() sections: any
  originalQuestion: any
  btnName: string
  questionExists: boolean
  loading: boolean
  isDuplicateDisplayOrder: boolean
  isIdEditable: boolean = false
  parentQuestionValid: boolean
  parentQuestionExists: boolean
  isExistingParentQuestion: boolean
  isParentQuestionLevelValid: boolean
  isParentQuestionDifferent: boolean
  parentYesNoAnswerExists: boolean
  parentDropdownAnswerExists: boolean
  selectedSection: any
  parentQuestion: any
  questionsCount: number
  sectionsCount: number
  isDuplicateDisplayOrderInSection: boolean
  isExistingQuestion: boolean
  questionQuestion: any

  constructor(public activeModal: NgbActiveModal, private questionService: QuestionService) {
  }

  ngOnInit(): void {
    this.originalQuestion = this.question;
    this.question = cloneDeep(this.question) || {};

    // If question already exists, update otherwise add
    if (this.question.question) {
      this.questionQuestion = this.question.question
      this.btnName = 'Update'
      this.questionExists = true;
      this.loading = false;
      this.isDuplicateDisplayOrder = false;
      this.isIdEditable = false;
      if (this.question.parentId) {
        this.parentQuestionValid = true;
        this.parentQuestionExists = true;
        this.isExistingParentQuestion = true;
        this.isParentQuestionLevelValid = true;
        this.isParentQuestionDifferent = true;

        if (this.question.parentAnswerToMatch === 'yes' || this.question.parentAnswerToMatch === 'no') {
          this.parentYesNoAnswerExists = true;
        } else {
          this.parentDropdownAnswerExists = true;
        }
      }
      if (this.question.sectionId) {
        this.selectedSection = (find(this.sections, {externalId: this.question.sectionId}));
      }
    } else {
      this.btnName = 'Add'
      this.questionQuestion = {}

      this.isIdEditable = true;
      let maxDisplayOrder = this.questions.reduce((x, order) => x = x > order.displayOrder ? x : order.displayOrder, 0);
      this.question.displayOrder = maxDisplayOrder + 1;
      this.parentQuestion = {};
    }

    this.questionsCount = this.questions.length;
    this.sectionsCount = this.sections.length;

    // If one section exists, all questions should belong to that section
    if (this.sectionsCount === 1) {
      this.onSectionSelect(this.sections[0]);
    }
  }

  trackByExternalId(index: number, obj: any): string {
    return obj.externalId;
  };

  onSectionSelect(section) {
    this.selectedSection = section;
    this.question.sectionId = section ? section.externalId : this.sections[0].externalId;
    let sectionQuestions = this.questions.filter(q => q.sectionId === this.question.sectionId);
    let maxDisplayOrder = sectionQuestions.reduce((x, order) => x = x > order.displayOrder ? x : order.displayOrder, 0);
    this.question.displayOrder = maxDisplayOrder + 1;
  }

  validateParentQuestionLevel(inQuestion, questions) {
    return this.questionService.getQuestionUpLevel(inQuestion.parentId, questions) + this.questionService.getQuestionDownLevel(inQuestion.question.id, questions) < 4
  }

  validateExistingQuestion(inQuestion) {
    return some(this.questions, (question) => {
      return question.question.id === inQuestion.id;
    });
  }

  validateDisplayOrder(displayOrder) {
    if (this.question.sectionId) {
      this.validateDisplayOrderInSection(displayOrder, this.question.sectionId)
    } else {
      this.isDuplicateDisplayOrder = some(this.questions, (question) => {
        return question.displayOrder === displayOrder && question != this.originalQuestion;
      });
    }
  }

  validateDisplayOrderInSection(displayOrder, sectionId) {
    let sectionQuestions = this.questions.filter(q => q.sectionId === sectionId);
    this.isDuplicateDisplayOrderInSection = some(sectionQuestions, (question) => {
      return question.displayOrder === displayOrder && question != this.originalQuestion;
    });
  }

  onQuestionIdChange(questionId) {
    if (questionId) {
      this.loading = true;
      this.questionService.getQuestion(questionId).subscribe(rsp => {
        this.question.question = rsp;
        this.questionQuestion = this.question.question;
        this.isExistingQuestion = this.validateExistingQuestion(this.question.question);
        this.questionExists = true;

      }, error => {
        this.questionExists = false;
        this.loading = false;
      });
    }
    this.questionExists = false;
  }

  addButtonEnabled() {
    return this.question.question && this.questionQuestion.id && this.question.question.text && this.question.displayOrder
      && !this.isExistingQuestion && !this.isDuplicateDisplayOrder
      && (!this.question.sectionId || (this.question.sectionId && !this.isDuplicateDisplayOrderInSection));
  }

  addQuestion() {
    this.question.requirement = 'mandatory'
    this.activeModal.close(this.question);
  }

}
