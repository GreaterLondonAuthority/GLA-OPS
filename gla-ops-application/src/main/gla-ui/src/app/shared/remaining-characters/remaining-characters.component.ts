import {Component, Input, OnInit} from '@angular/core';

@Component({
  selector: 'gla-remaining-characters',
  templateUrl: './remaining-characters.component.html',
  styleUrls: ['./remaining-characters.component.scss']
})
export class RemainingCharactersComponent implements OnInit {
  @Input() text: string
  @Input() max: number

  constructor() {
  }

  ngOnInit(): void {
  }

}
