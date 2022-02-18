import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {environment} from "../../../environments/environment";
import {groupBy, isArray, sortBy, startCase} from "lodash-es";

@Injectable({
  providedIn: 'root'
})
export class QuestionService {

  constructor(private http: HttpClient) {
  }

  getQuestion(id) {
    return this.http.get(`${environment.basePath}/questions/${id}?enrich=true`)
  }

  getQuestionUpLevel(inQuestionId, questions) {
    let returnLevel = 0
    questions.forEach(question => {
      if (question.question.id == inQuestionId) {
        if (question.parentId) {
          returnLevel = 1 + this.getQuestionUpLevel(question.parentId, questions)
        } else {
          returnLevel = 1;
        }
      }
    });
    return returnLevel
  }

  getQuestionDownLevel(inQuestionId, questions) {
    let returnLevel = 0
    questions.forEach(question => {
      if (question.parentId == inQuestionId) {
        returnLevel = 1 + this.getQuestionDownLevel(question.question.id, questions)
      }
    });
    return returnLevel
  }

}
