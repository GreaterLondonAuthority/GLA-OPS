import {Component, Input, OnInit} from '@angular/core';
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {cloneDeep, indexOf} from "lodash-es";
import isEqual from "lodash-es/isEqual";

@Component({
  selector: 'gla-project-assign-modal',
  templateUrl: './project-assign-modal.component.html',
  styleUrls: ['./project-assign-modal.component.scss']
})
export class ProjectAssignModalComponent implements OnInit {

  @Input() projects: any[]
  @Input() assignableUsers: any
  @Input() unassign: boolean
  originalAssignableUsers:any
  managingOrgIds: number[]

  constructor(public activeModal: NgbActiveModal) { }

  ngOnInit(): void {
    this.originalAssignableUsers =  cloneDeep(this.assignableUsers || []);
    this.managingOrgIds = [...new Set(this.projects.map(p => p.managingOrganisationId))]
  }

  disableSave() {
    return !this.hasModalChanges() || this.hasNoUsers()
  }

  hasNoUsers() {
    if (this.unassign) {
      return (this.assignableUsers || []).filter(option => !option.model).length < 1
    } else {
      return (this.assignableUsers || []).filter(option => !!option.model).length < 1
    }
  }

  hasModalChanges() {
    return !isEqual(this.originalAssignableUsers, this.assignableUsers)
  }

  save() {
    if (this.unassign) {
      this.performUnassignment()
    } else {
      this.performAssignment()
    }
  }

  performAssignment() {
    let assignees = (this.assignableUsers || []).filter(option => !!option.model).map(type => type.id);
    let assigneeNames = (this.assignableUsers || []).filter(option => !!option.model).map(type => type.label);
    this.projects.forEach(project => {
      project.assignee = this.addValuesToPipeSeparatedString(project.assignee, assignees)
      project.assigneeName = this.addValuesToPipeSeparatedString(project.assigneeName, assigneeNames)
    });
    this.activeModal.close();
  }

  performUnassignment() {
    let assigneesToRemove = (this.assignableUsers || []).filter(option => !option.model).map(type => type.id);
    let assigneeNamesToRemove = (this.assignableUsers || []).filter(option => !option.model).map(type => type.label);
    this.projects.forEach(project => {
      project.assignee = this.removeValuesFromPipeSeparatedString(project.assignee, assigneesToRemove)
      project.assigneeName = this.removeValuesFromPipeSeparatedString(project.assigneeName, assigneeNamesToRemove)
      project.assigneesToRemove = assigneesToRemove
      project.assigneeNamesToRemove = assigneeNamesToRemove
    });
    this.activeModal.close();
  }

  addValuesToPipeSeparatedString(psString: string, values: string[]): string {
    let currentValues = psString? psString.split('|'): []
    values.forEach((value: string) => {
      if (!currentValues.includes(value)) {
        currentValues.push(value)
      }
    })
    return currentValues.join('|')
  }

  removeValuesFromPipeSeparatedString(psString: string, values: string[]): string {
    let currentValues = psString? psString.split('|'): []
    values.forEach((value: string) => {
      while (currentValues.includes(value)) {
        currentValues.splice(currentValues.indexOf(value),1)
      }
    })
    return currentValues.join('|')
  }

}
