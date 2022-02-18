import {Component, Input, OnInit} from '@angular/core';
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {QuestionService} from "../template-block-questions/question.service";
import {cloneDeep, some, sortBy} from "lodash-es";

@Component({
  selector: 'gla-section-modal',
  templateUrl: './section-modal.component.html',
  styleUrls: ['./section-modal.component.scss']
})
export class SectionModalComponent implements OnInit {

  @Input() section: any
  @Input() sections: any
  @Input() block: any
  originalSection: any
  isUpdate: boolean
  btnName: string
  isDuplicateDisplayOrder: boolean = false
  isDuplicateExternalId: boolean
  isIdEditable: boolean


  constructor(public activeModal: NgbActiveModal, private questionService: QuestionService) {
  }

  ngOnInit(): void {
    this.sections = this.sections || [];
    this.originalSection = this.section;
    this.section = cloneDeep(this.section);
    this.block = this.block;
    this.isUpdate = this.section && this.section.displayOrder != undefined;
    // If section already exists, update otherwise add
    if (this.isUpdate) {
      this.btnName = 'Update';
      this.isDuplicateDisplayOrder = false;
      this.isDuplicateExternalId = false;
      // $timeout(()=>{
      //   this.onParentQuestionIdChange(this.section, true);
      // });
    } else {
      this.btnName = 'Add';
      this.section.id = null;
      this.isIdEditable = true;
      //let max = maxBy(this.sections, 'displayOrder' || {});

      let maxDisplayOrder = this.sections.reduce((acc, shot) => acc = acc > shot.displayOrder ? acc : shot.displayOrder, 0);
      this.section.displayOrder = maxDisplayOrder + 1;
    }
  }

  validateDisplayOrder(displayOrder) {
    this.isDuplicateDisplayOrder = some(this.sections, (section) => {
      return section.displayOrder === displayOrder && section != this.originalSection;
    });
  }

  validateExternalId(externalId) {
    this.isDuplicateExternalId = some(this.sections, (section) => {
      return section.externalId === externalId && section != this.originalSection;
    });
  }

  addButtonEnabled() {
    // (!this.section.parentId || this.parentQuestionValid && this.section.parentAnswerToMatch)
    return this.section.text && this.section.displayOrder && !this.isDuplicateDisplayOrder && this.section.externalId && !this.isDuplicateExternalId
  }

  addSection() {
    this.activeModal.close(this.section);
  }

  onMultiSelectChange(question) {
    let sortedAnswers = sortBy(question.answerOptions, 'displayOrder')
    let answers = (sortedAnswers || []).filter(ao => !!ao.model);
    this.section.parentAnswerToMatch = answers.map(ao => ao.option).join(question.delimiter)
  }
}
