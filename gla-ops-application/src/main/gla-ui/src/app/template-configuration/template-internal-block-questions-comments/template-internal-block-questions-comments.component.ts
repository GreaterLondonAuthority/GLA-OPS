import {Component, Injector, Input, OnInit} from '@angular/core';
import {filter, find, groupBy, remove, sortBy} from "lodash-es";
import {ConfirmationDialogService} from "../../shared/confirmation-dialog/confirmation-dialog.service";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {SectionModalComponent} from "../section-modal/section-modal.component";
import {QuestionModalComponent} from "../question-modal/question-modal.component";
import {TemplateConfigurationService} from "../template-configuration.service";
import {ToastrUtilService} from "../../shared/toastr/toastr-util.service";
import {TemplateService} from "../../template/template.service";
import {NavigationService} from "../../navigation/navigation.service";
import {ReferenceDataService} from "../../reference-data/reference-data.service";

@Component({
  selector: 'gla-template-internal-block-questions-comments',
  templateUrl: './template-internal-block-questions-comments.component.html',
  styleUrls: ['./template-internal-block-questions-comments.component.scss']
})
export class TemplateInternalBlockQuestionsCommentsComponent implements OnInit {

  @Input() readOnly: boolean
  @Input() editable: boolean;
  @Input() block: any
  @Input() draft: boolean
  @Input() $state: any;
  sections: any
  questions: any
  sortedQuestions: any
  sortedSections: any
  questionsWithoutSection: any
  question: any
  showExpandAll: boolean
  requirements: any
  confirmationDialog: ConfirmationDialogService;
  removeQuestionCommand: any
  errorMsg: any

  constructor(private injector: Injector, private ngbModal: NgbModal,
              private navigationService: NavigationService,
              private templateConfigurationService: TemplateConfigurationService,
              private toastrUtil: ToastrUtilService,
              private templateService: TemplateService,
              private referenceDataService: ReferenceDataService) {
    this.confirmationDialog = injector.get(ConfirmationDialogService);
    this.$state = this.navigationService.getCurrentStateParams()
  }

  ngOnInit(): void {
    this.requirements = this.referenceDataService.getRequirementOptions()

    this.removeQuestionCommand = filter(this.block.templateBlockCommands, {name: 'REMOVE_QUESTION'});
    this.question = this.question || {answerOptions: [{}, {}]};
    this.block.questions =  this.block.questions || []
    this.block.sections = this.block.sections || [];
    this.sections = this.block.sections;
    this.questions = this.block.questions;
    this.refreshQuestions();
  }

  trackByQuestionId(index: number, question: any): number {
    return question.question.id;
  }

  toggle(section: any) {
    section.collapsed = !section.collapsed;
  }

  refreshQuestions() {
    this.questions = sortBy(this.questions, 'displayOrder');
    this.questionsWithoutSection = this.questions.filter(q => !q.sectionId)

    let questionsWitSection = this.questions.filter(q => q.sectionId)
    this.sortedQuestions = groupBy(questionsWitSection, 'sectionId');
    this.refreshSections();
  }

  refreshSections() {
    this.sortedSections = sortBy(this.sections, 'displayOrder');
  }

  isParentQuestion(id) {
    let parentIds = this.questions.map(q => +q.parentId).filter(item => item !== undefined);
    return parentIds.indexOf(id) !== -1;
  }

  updateSections() {
    this.block.sections = this.sortedSections;
    this.refreshSections();
  }

  updateQuestions() {
    this.block.questions = this.questions
    this.refreshQuestions()
  }

  getSectionText(sectionId) {
    if (sectionId == 'undefined') {
      return 'No section specified'
    }
    let section = find(this.block.sections, {externalId: +sectionId});
    return section.text;
  }

  isSectionEmpty(section) {
    return find(this.questions, {sectionId: section.externalId}) ? false : true;
  }

  showQuestionModal(question?: any) {
    let existingQuestion = question ? find(this.questions, {question: question.question}) : null;
    let modal = this.ngbModal.open(QuestionModalComponent);
    modal.componentInstance.question = question || {};
    modal.componentInstance.questions = this.questions;
    modal.componentInstance.sections = this.sections;
    modal.result.then((question) => {
      if (question) {
        if (existingQuestion) {
          let questionsIndex = this.questions.indexOf(existingQuestion);
          let blockQuestionsIndex = this.block.questions.indexOf(find(this.block.questions, existingQuestion));
          this.questions[questionsIndex] = question;
          this.block.questions[blockQuestionsIndex] = question;
        } else {
          this.questions.push(question);
          this.block.questions.push(question);
        }

        this.refreshQuestions();
      } else {
        alert('Can\'t add to invalid template JSON');
      }
    }, () => {
    });
  }

  showSectionModal(section?: any) {
    let existingSection = section ? find(this.sections, {displayOrder: section.displayOrder}) : null;
    let modal = this.ngbModal.open(SectionModalComponent);
    modal.componentInstance.section = section || {};
    modal.componentInstance.sections = this.sections;
    modal.componentInstance.block = this.block;
    modal.result.then((section) => {
      if (section) {
        if (existingSection) {
          let sectionsIndex = this.sections.indexOf(existingSection);
          this.sections[sectionsIndex] = section;
          let blockSectionsIndex = this.block.sections.indexOf(find(this.block.sections, existingSection));
          this.block.sections[blockSectionsIndex] = section;
        } else {
          this.sections.push(section);
          this.block.sections.push(section);
          this.moveQuestionsToInitialSection(this.sections.length, section.externalId)
        }
        this.refreshSections();
        this.refreshQuestions();
      } else {
        alert('Can\'t add to invalid template JSON');
      }
    }, () => {
    });
  }

  moveQuestionsToInitialSection(sectionsLength, sectionExternalId) {
    if (sectionsLength === 1) {
      this.questions.forEach(q => {
        q.sectionId = sectionExternalId;
      });
    }
  }

  deleteQuestion(question: any) {
    let modal = this.confirmationDialog.delete('Are you sure you want to delete question ' + question.question.id + '?');
    modal.result.then(() => {
      remove(this.questions, question);
      remove(this.block.questions, question);
      this.refreshQuestions();

    });
  }

  deleteSection(section: any) {
    let modal = this.confirmationDialog.show({
      message: 'Are you sure you want to delete ' + section.text + ' section?',
      title: 'Delete Section',
      approveText: 'DELETE',
      dismissText: 'KEEP'
    });
    modal.result.then(() => {
      remove(this.sections, section);
      remove(this.block.sections, section);
      this.refreshSections();

    });
  }

  getWarningMessage(block, message) {
    return message.replace('[block_name]', block.blockDisplayName);
  }

  performAction(command: any, block: any) {
    let blockData = {
      blockId: this.block.id,
      blockType: this.block.type,
      blockOldName: this.block.blockDisplayName,
      infoMessage: this.block.infoText,
      questionId: block.questionId,
      displayOrder: this.block.displayOrder
    }
    command.payload = { blockData}
    command.displayOrder = this.block.displayOrder;
    command.internalBlock = true;

    let modal = this.confirmationDialog.show({
      message: this.getWarningMessage(block, command[0].warningMessage),
      existingComment: '',
      approveText: command[0].title.toUpperCase(),
      dismissText: 'CANCEL',
      userCommentRequired: command[0].requiresComment,
      userCommentLabel: 'Reason for removal',
      maxCommentLength: command[0].maxCommentLength
    });

    modal.result.then((userInput) => {
      if (command[0].requiresComment){
        this.templateConfigurationService.setUserInputComment(command[0], blockData, userInput)
      }
      this.performCommand(command)
    }, (error) => {});
  }

  performCommand(command: any) {
    this.templateService.performCommand(this.$state.templateId, command.internalBlock, command.displayOrder, command[0].name, command.payload || {}).toPromise().then((resp:any) => {
      this.toastrUtil.success(`Template updated`);
      this.errorMsg = null;
      let updatedBlock = filter(resp.internalBlocks,{type:'Questions', displayOrder: command.displayOrder})
      this.questions = updatedBlock[0].questions;
      this.refreshQuestions();
    }, (resp) => {
      this.errorMsg = resp.error? resp.error.description : resp.data.description;
    });
  }

}
