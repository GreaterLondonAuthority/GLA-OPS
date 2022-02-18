import {Component, Input, OnInit} from '@angular/core';

@Component({
  selector: 'gla-confirmation',
  templateUrl: './confirmation.component.html',
  styleUrls: ['./confirmation.component.scss']
})
export class ConfirmationComponent implements OnInit {

  @Input() title: string
  @Input() text: string

  constructor() { }

  ngOnInit(): void {
  }

}
