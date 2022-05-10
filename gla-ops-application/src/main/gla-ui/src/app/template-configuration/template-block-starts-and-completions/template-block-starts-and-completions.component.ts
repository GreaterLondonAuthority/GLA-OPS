import {Component, Input, OnInit} from '@angular/core';

@Component({
  selector: 'gla-template-block-starts-and-completions',
  templateUrl: './template-block-starts-and-completions.component.html',
  styleUrls: ['./template-block-starts-and-completions.component.scss']
})
export class TemplateBlockStartsAndCompletionsComponent implements OnInit {

  @Input() block
  @Input() template
  @Input() readOnly : boolean
  @Input() editable : boolean

  items:  string[] = ['Acquisitions', 'SpecialisedAndSupportedHousing']


  constructor() { }

  ngOnInit(): void {
    if (!this.block.ofWhichCategories) {
      this.block.ofWhichCategories = [];
    }
  }

  getOfWhichCategoryDisplayName(category: string) {
    if (category == 'SpecialisedAndSupportedHousing') {
      return 'Specialised & Supported Housing';
    } else {
      return category;
    }
  }

  isItemSelected(item: string) {
    return this.block.ofWhichCategories.indexOf(item) != -1;
  }

  toggleSelected(item: string) {
    let position = this.block.ofWhichCategories.indexOf(item);
    if (position != -1) {
      this.block.ofWhichCategories.splice(position, 1)
    } else {
      this.block.ofWhichCategories.push(item)
    }
  }
}
