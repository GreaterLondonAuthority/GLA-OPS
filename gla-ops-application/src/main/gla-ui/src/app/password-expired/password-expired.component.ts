import {Component, Input, OnInit} from '@angular/core';

@Component({
  selector: 'gla-password-expired',
  templateUrl: './password-expired.component.html',
  styleUrls: ['./password-expired.component.scss']
})
export class PasswordExpiredComponent implements OnInit {

  constructor() { }

  @Input() username : String;

  ngOnInit(): void {
  }

}
