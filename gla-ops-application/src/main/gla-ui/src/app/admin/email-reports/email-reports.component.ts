import {Component, Input, OnInit} from '@angular/core';
import {NotificationService} from "./notification.service";


@Component({
  selector: 'gla-email-reports',
  templateUrl: './email-reports.component.html',
  styleUrls: ['./email-reports.component.scss']
})
export class EmailReportsComponent implements OnInit {

  @Input() emails: any;
  totalItems : any;
  itemsPerPage : any
  currentPage : any
  searchOptions : any
  selectedSearchOption : any
  searchText: string

  constructor(private notificationService: NotificationService) { }

  ngOnInit(): void {
    this.initSearchDropdown();
    this.itemsPerPage = 50;
    this.currentPage = 1;
    this.getEmails();
  }

  initSearchDropdown() {
    this.searchOptions = [
      {
        name: 'text',
        description: 'By Recipient',
        hint: 'Search by email recipient',
        maxLength: '255'
      },
      {
        name: 'text',
        description: 'By Subject',
        hint: 'Search by email subject',
        maxLength: '255'
      },
      {
        name: 'text',
        description: 'By Body text',
        hint: 'Search by email body',
        maxLength: '255'
      }
    ];
    this.selectedSearchOption = this.searchOptions[0];
    this.searchText = null;
  }

  select(searchOption) {
    this.searchText = null;
    this.selectedSearchOption = searchOption;
  };

  clearSearchText() {
    this.searchText = null;
    this.getEmails();
  }

  getEmails() {
    let recipientText = (this.searchOptions.indexOf(this.selectedSearchOption) === 0) ? this.searchText : null;
    let subjectText = (this.searchOptions.indexOf(this.selectedSearchOption) === 1) ? this.searchText : null;
    let bodyText = (this.searchOptions.indexOf(this.selectedSearchOption) === 2) ? this.searchText : null;

    let config : any = {
      params: {
        recipient: recipientText,
        subject: subjectText,
        body: bodyText,
        page: this.currentPage - 1,
        size: this.itemsPerPage,
        sort: 'date,desc'
      }
    };
    this.notificationService.getAllEmailsByRecipientAndSubjectAndBody(config).subscribe((rsp : any) => {
      this.emails = rsp.content;
      this.totalItems = rsp.totalElements;
    });
  }

}
