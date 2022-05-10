import { Component, OnInit, Input } from '@angular/core';
import { QuestionsService } from '../questions.service';
import { LoadingMaskService } from '../../shared/loading-mask/loading-mask.service';

@Component({
  selector: 'gla-questions2',
  templateUrl: './questions.component.html',
  styleUrls: ['./questions.component.scss']
})
export class QuestionsComponent implements OnInit {
  @Input() questions: any
  @Input() block: any
  @Input() project: any
  @Input() readOnly: boolean
  uploadParams: any

  constructor(private questionsService: QuestionsService,
              private loadingMaskService: LoadingMaskService) {

   }

  ngOnInit(): void {
    this.uploadParams = {
      orgId: this.project.organisation.id,
      programmeId: this.project.programmeId,
      projectId: this.project.id
    }
    for (let question of this.questions) {
      if (question.answerType === 'Dropdown' && !question.answer) {
        question.answer = 'Not provided'
      }
      if (question.answerType === 'FileUpload' && !question.fileAttachments) {
        question.fileAttachments = []
        question.totalAttachmentsSize = 0
      }
    }
    console.log('Questions:', this.questions)
  }

  isVisible(question) {
    return this.questionsService.isQuestionVisible(question, this.questions);
  }

  updateSectionVisibility(){
    this.questionsService.updateSectionVisibility(this.questions);
  }

  onMultiSelectChange(check, question) {
    let answers = (question.answerOptions || []).filter(ao => !!ao.model);
    question.answer = answers.map(ao => ao.label).join(question.delimiter);
    this.updateSectionVisibility();
  }

  formatDropdownAnswer(question){
    if(!question || !question.answer){
      return null;
    }
    return question.answer.split(question.delimiter).join(question.delimiter + ' ');
  }

  trackByQuestion(el:any): number {
    return el.id;
  }

  getPostUrl(question:any):string {
    return '/questions/'+ question.id + '/file?programmeId=' + this.project.programmeId + '&projectId=' + this.project.id + '&blockId=' + this.block.id
  }

  onFileUploadComplete(event: any, question: any) {
    let file = event.response
    question.fileAttachments.push(file)
    question.totalAttachmentsSize += file.fileSize
    this.loadingMaskService.showLoadingMask(false)
  }

  onFileRemoval(event: any, question: any) {
    let file = event.response
    let index = question.fileAttachments.indexOf(file)
    question.fileAttachments.splice(index, 1)
    question.totalAttachmentsSize -= file.fileSize
  }

}
