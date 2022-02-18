import {Component, Input, OnInit} from '@angular/core';
import {FinanceService} from "./finance-categories/finance-service.service";
import {OutputCategoryService} from "./output-categories/output-category.service";
import {ReferenceDataService} from "../reference-data/reference-data.service";

@Component({
  selector: 'gla-categories',
  templateUrl: './categories.component.html',
  styleUrls: ['./categories.component.scss']
})
export class CategoriesComponent implements OnInit {

  @Input() stateParams: any;
  @Input() state: any;
  @Input() financeCategories: any[];
  @Input() outputCategories: any[];
  @Input() budgetCategories: any;
  activeTabIndex: any
  tabs: any
  searchOptions : any
  selectedSearchOption : any
  searchText: string

  constructor(private financeService : FinanceService,
              private outputCategoryService : OutputCategoryService,
              private referenceDataService : ReferenceDataService) { }

  ngOnInit(): void {
    this.tabs = {
      budgetCategories: 1,
      financeCategories: 2,
      outputCategories: 3
    };
    this.activeTabIndex = 1;
    this.searchOptions = [{
        name: 'text',
        description: 'Category Name',
        hint: 'Search by category name',
        maxLength: '255'
      }];
    this.selectedSearchOption = this.searchOptions[0];
  }

  onNavChange(tabIndex) {
    this.activeTabIndex = tabIndex;
  }

  select(searchOption) {
    this.searchText = null;
    this.selectedSearchOption = searchOption;
  };

  clearSearchText() {
    this.searchText = null;
    this.search();
  }

  search() {
    let config : any = {
      params: {
        category: this.searchText
      }
    };

    this.financeService.getFinanceCategories(true, config).subscribe((rsp : any) => {
      this.financeCategories = rsp;
    });
    this.outputCategoryService.getAllOutputConfiguration(config).subscribe((rsp : any) => {
      this.outputCategories = rsp;
    });
    this.referenceDataService.getConfigItemsByType('BudgetCategories', config).subscribe((rsp : any) => {
      this.budgetCategories = rsp;
    });
  }

}
