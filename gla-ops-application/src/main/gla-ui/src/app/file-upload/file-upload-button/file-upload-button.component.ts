import { Component, EventEmitter, ElementRef, Input, OnInit, Output, ViewChild } from '@angular/core';
import { environment } from '../../../environments/environment';
import * as _ from 'lodash';

@Component({
  selector: 'gla-file-upload-button',
  templateUrl: './file-upload-button.component.html',
  styleUrls: ['./file-upload-button.component.scss']
})
export class FileUploadButtonComponent implements OnInit {
  @ViewChild('fileUploadInput') fileUploadInput: ElementRef
  @Input() remainingCombinedFileSize: number
  @Input() maxCombinedUploadSizeInMb: number
  @Input() maxFileSizeInMb: number
  @Input() uploadParams: any
  @Input() label: any
  @Input() isDisabled: boolean
  @Input() componentInputId: any
  @Input() postUrl: any
  @Input() context: any
  @Input() attachments: any[] = []
  @Input() cls: any
  @Output() onError: EventEmitter<any> = new EventEmitter()
  @Output() onProgress: EventEmitter<any> = new EventEmitter()
  @Output() onComplete: EventEmitter<any> = new EventEmitter()
  MAX_FILE_SIZE_MO : any
  URL_PREFIX : any
  MAX_FILE_SIZE : any
  POST_URL : any
  uniqueId: string

  constructor() { }

  ngOnInit(): void {
    console.log('cls', this.cls)
    this.MAX_FILE_SIZE_MO = this.maxCombinedUploadSizeInMb || this.maxFileSizeInMb || 5;
    this.MAX_FILE_SIZE = this.MAX_FILE_SIZE_MO * 1024;
    this.URL_PREFIX = environment.basePath;
    this.POST_URL = this.postUrl || '/file';
    this.uniqueId = this.componentInputId ? 'container-' + this.componentInputId : 'container-file-upload'

    //TODO wait for rendering in a better way
    setTimeout(() => {
      const input = this.getFileUploadInput()
      input.addEventListener('change', this.handleSelected.bind(this))
    }, 1000)
  }

  getFileUploadInput() {
    return this.fileUploadInput.nativeElement
  }

  openFileDialog() {
    this.getFileUploadInput().click()
  }

  handleSelected(e: any) {
    const list = e.target.files;

    //IE issue. Called twice.
    if( list.length === 0 ){
      return;
    }

    const exceedLimit = this._checkSizeLimit(list);
    if (exceedLimit.length) {
      const exceedNames = _.map(exceedLimit, file => {
        return file.name;
      });
      const msg = {
        title: 'Document over maximum allowed size',
        description: this.maxCombinedUploadSizeInMb ? `Combined file size exceeds the combined file size limit (${this.maxCombinedUploadSizeInMb}MB)`
          : `The file ${exceedNames.toString()} exceeds the supported size of ${this.maxCombinedUploadSizeInMb || this.maxFileSizeInMb || 5}MB. Add a smaller document.`
      }
      this._onError(msg);
    } else {
      this.uploadFile(list[0]);
    }
  }

  uploadFile(file: any) {
    let reader = new FileReader();
    let xhr = new XMLHttpRequest();

    xhr.upload.addEventListener('progress', this._onProgress.bind(this));
    // xhr.upload.addEventListener('loadend', this._onComplete.bind(this)); //using readyState
    xhr.upload.addEventListener('error', this._onError.bind(this));

    xhr.addEventListener('readystatechange', this._onReadyStateChange.bind(this));

    //data object to send
    let data = new FormData();
    data.append('file', file);
    for (let prop in this.uploadParams) {
      data.append(prop, this.uploadParams[prop]);
    }
    xhr.open('POST', `${this.URL_PREFIX  + this.POST_URL}`);
    xhr.send(data);
  };

  _onReadyStateChange(e: any) {
    const state = e.target.readyState;
    if (state === XMLHttpRequest.DONE && (e.target.status >= 200 && e.target.status < 300)) {
      this._onComplete(e.target.responseText);
    } else if (state === XMLHttpRequest.DONE && (e.target.status < 200 || e.target.status >= 300)) {
      this._onError({
        description: JSON.parse(e.target.responseText).description
      });
    } else if (state === XMLHttpRequest.UNSENT) {
      this._onError(e.target.responseText);
    }
  }

  _checkSizeLimit(list: any[]) {
    return _.reject(list, file => {
      if (this.remainingCombinedFileSize) {
        return file.size <= this.remainingCombinedFileSize;
      }
      else {
        return ((file.size / 1024) < this.MAX_FILE_SIZE);
      }
    });
  }

  _onError(e: any) {
    if (this.onError) {
      this.onError.emit({
        error: e,
        context: this.context
      })
    }
  }

  _onProgress(e: any) {
    if (e.lengthComputable) {
      const percent = Math.round((e.loaded * 100) / e.total);
      if (percent <= 100) {
        if (this.onProgress) {
          this.onProgress.emit({
              progress: percent,
              context: this.context
          })
        }
      }
    }
  }

  _onComplete(e: any) {
    const resp = JSON.parse(e);

    // this.fileUploadInput.nativeElement.val(''); //reset filelist from input

    if (this.onComplete) {
      this.onComplete.emit({
          response: resp
      });
    }
  }



}
