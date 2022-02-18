import {Component, Input, OnInit} from '@angular/core';
import {sortBy} from "lodash-es";
import {OutputCategoryModalComponent} from '../output-category-modal/output-category-modal.component';
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {OutputCategoryService} from "../output-category.service";
import {ToastrUtilService} from "../../../shared/toastr/toastr-util.service";

@Component({
  selector: 'gla-output-categories',
  templateUrl: './output-categories.component.html',
  styleUrls: ['./output-categories.component.scss']
})
export class OutputCategoriesComponent implements OnInit {

  @Input() outputConfigurations;

  category;
  groupId;

  constructor(private ngbModal: NgbModal, private toastrUtil: ToastrUtilService, private outputCatService : OutputCategoryService) {

  }

  ngOnInit(): void {
    this.sort();
  }

  sort(): void {
    this.outputConfigurations = sortBy(this.outputConfigurations, ['category', 'subcategory', 'id']);
  }

  addCategory() {
    const modal = this.ngbModal.open(OutputCategoryModalComponent, { size: 'md' });
    modal.componentInstance.model = new Object();
    modal.componentInstance.model.category = this.category;
    modal.componentInstance.model.groupId = this.groupId;
    modal.result.then((block) => {
      this.toastrUtil.success('New output category successfully added.');
      this.category = block.category;
      this.groupId = block.groupId;
      this.outputCatService.getAllOutputConfiguration({}).subscribe((categories)=> {
          this.outputConfigurations = categories;
          this.sort();
        }
      )
    }, ()=>{});
  }

  success() {

  }

  failure() {

  }
}
