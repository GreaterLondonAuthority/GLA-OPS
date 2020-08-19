import {Component, Input, OnInit, OnChanges} from '@angular/core';
import StringUtil from '../../../../../ui/app/scripts/util/StringUtil.js';

@Component({
  selector: 'gla-markdown',
  templateUrl: './markdown.component.html',
  styleUrls: ['./markdown.component.scss']
})
export class MarkdownComponent implements OnInit, OnChanges {

  @Input() text: string
  html: string;

  constructor() { }

  ngOnInit(): void {
  }

  ngOnChanges(): void {
    if(this.text) {
      let textWithoutHtml = StringUtil.removeHtml(this.text);
      this.html = StringUtil.replaceMarkdownUrl(textWithoutHtml);
    }
  }
}
