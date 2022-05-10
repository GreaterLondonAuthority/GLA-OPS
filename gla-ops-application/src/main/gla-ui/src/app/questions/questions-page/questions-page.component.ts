import { Component, OnInit, Injector, Input } from '@angular/core';
import { ProjectBlockComponent } from '../../project-block/project-block.component';
import { QuestionsService } from '../questions.service';
import { ProjectService } from '../../project/project.service';

@Component({
  selector: 'gla-questions-page',
  templateUrl: './questions-page.component.html',
  styleUrls: ['./questions-page.component.scss']
})
export class QuestionsPageComponent extends ProjectBlockComponent implements OnInit {
  @Input() projectBlock: any;
  @Input() template: any;
  @Input() history: any;
  @Input() isBlockRevertEnabled: any;
  questionsService: QuestionsService;
  projectService: ProjectService
  questions: any;

  constructor(injector: Injector) {
    super(injector);
    this.questionsService = injector.get(QuestionsService)
  }

  ngOnInit(): void {
    super.ngOnInit()
    console.log(this.project)
    this.questions = this.questionsService.getQuestionsFromBlock(this.projectBlock)
  }

  back() {
    if (this.readOnly) {
      this.returnToOverview();
    } else {
      this.submit();
    }
  }

  submit() {
    this.projectBlock.answers = this.questionsService.getAnswers(this.questions);
    return this.projectService.updateProjectAnswers(this.project.id, this.blockId, this.projectBlock)

  }

}
