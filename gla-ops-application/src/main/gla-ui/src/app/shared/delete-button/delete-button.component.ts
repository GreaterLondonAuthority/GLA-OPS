import {Component, Input, OnInit} from '@angular/core';

@Component({
  selector: 'delete-button',
  templateUrl: './delete-button.component.html',
  styleUrls: ['./delete-button.component.scss']
})
export class DeleteButtonComponent implements OnInit {

  @Input() text: string
  constructor() { }

  ngOnInit(): void {
  }
}

