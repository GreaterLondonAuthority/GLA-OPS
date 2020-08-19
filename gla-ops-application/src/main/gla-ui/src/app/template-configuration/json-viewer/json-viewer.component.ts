import {Component, EventEmitter, Input, OnInit, OnChanges, Output, ViewChild, SimpleChanges} from '@angular/core';
import {JsonEditorComponent, JsonEditorOptions} from "ang-jsoneditor";

@Component({
  selector: 'gla-json-viewer',
  templateUrl: './json-viewer.component.html',
  styleUrls: ['./json-viewer.component.scss']
})
export class JsonViewerComponent implements OnInit, OnChanges {
  @Input() json: any;
  @Input() readOnly: boolean;

  @Output() change = new EventEmitter<string>();

  @ViewChild('editor') editor: JsonEditorComponent;

  options: JsonEditorOptions;
  hideEditor = false;

  constructor() { }

  ngOnInit(): void {
    this.options = new JsonEditorOptions()
    this.options.mode = 'code';
    this.options.onChange = () => {
      this.change.emit(this.editor.getText())
    }

    this.options.onEditable = (node) => {
      return !this.readOnly;
    }
  }

  ngOnChanges(changes: SimpleChanges) {
    if(changes.readOnly){
    }
  }
}
