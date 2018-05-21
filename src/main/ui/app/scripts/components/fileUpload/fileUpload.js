/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

/**
 * File upload generic component.
 *
 * Accepts the following HTML parameters:
 * @param {String} `label` - button label
 * @param {Object} `upload-params` - upload object parameters i.e. `{orgId: 10000}`
 * @param {Function} `on-progress` - returns an integer of the progress percentage
 * @param {Function} `on-complete` - returns the server response on completion
 * @param {Function} `on-error` - returns an error message
 * @param {Boolean} `is-disabled` - toggle enable/disabled state
 */
class FileUploadCtrl {
  constructor(config, $scope, $timeout, $log, $element) {
    var self = this;
    this.MAX_FILE_SIZE_MO = 5;
    this.URL_PREFIX = config.basePath;
    this.MAX_FILE_SIZE = this.MAX_FILE_SIZE_MO * 1024;

    this.$scope = $scope;
    this.$log = $log;
    this.$element = $element;
    this._id = self.componentInputId ?(self.componentInputId): 'fileInput';

    // this.multiple = this.multiple ? this.multiple : false; //not working yet

    // controlling disabled state in code, due to a bug in the handling in html

    // offset bidding to ensure element is rendered (mainly a issue with unit test)
    $timeout(function() {
      self.$log.log(self._id);
      self.fileInputEl = self.getFileInput();
      self.fileInput = self.fileInputEl[0];
      self.fileInput.addEventListener('change', self.handleSelected.bind(self));
    });

  }

  getFileInput(){
    return this.$element.find('#'+this._id +'-upload-input');
  }

  /**
   * Check if any file is over the filesize limit
   * @return {Array} list of files over limit
   */
  _checkSizeLimit(list) {
    return _.reject(list, file => {
      return ((file.size / 1024) < this.MAX_FILE_SIZE);
    });
  }

  /**
   * Upload progress handler
   * @return {Object} response
   */
  _onProgress(e) {
    if (e.lengthComputable) {
      const percent = Math.round((e.loaded * 100) / e.total);
      if (percent <= 100) {
        // this.$log.debug(`uploading: ${percent}%`);
        if (this.onProgress) {
          this.$scope.$evalAsync(() => {
            this.onProgress({
              event: {
                progress: percent,
                context: this.context
              }
            });
          });
        }
      }
    }
  }

  /**
   * Upload completion handler
   * @return {Number} progress percentage
   */
  _onComplete(e) {
    const resp = JSON.parse(e);
    this.$log.log('uploading: complete', resp);

    this.fileInputEl.val(''); //reset filelist from input

    if (this.onComplete) {
      this.$scope.$evalAsync(() => {
        this.onComplete({
          event: {
            response: resp,
            context: this.context
          }
        });
      });
    }
  }

  /**
   * Upload error handler
   * @return {Object} response
   */
  _onError(e) {
    // this.$log.log(e);
    if (this.onError) {
      this.$scope.$evalAsync(() => {
        this.onError({
          event: {
            error: e,
            context: this.context
          }
        });
      });
    }
  }

  /**
   * XMLHttpRequest ready state handler
   * @param {Object} e
   */
  _onReadyStateChange(e) {
    const state = e.target.readyState;
    if (state === XMLHttpRequest.DONE && (e.target.status >= 200 && e.target.status < 300)) {
      this._onComplete(e.target.responseText);
    } else if (state === XMLHttpRequest.DONE && (e.target.status < 200 || e.target.status >= 300)) {
      this._onError({
        copy: JSON.parse(e.target.responseText).description
      });
    } else if (state === XMLHttpRequest.UNSENT) {
      this._onError(e.target.responseText);
    }
  }

  /**
   * File upload
   * @param {Object} file
   */
  uploadFile(file) {
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
    };
    xhr.open('POST', `${this.URL_PREFIX}/file`);
    xhr.send(data);
  };

  /**
   * Handle selected files
   * @param {Object} e
   */
  handleSelected(e) {
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
        copy: `The file ${exceedNames.toString()} exceeds the supported size of ${this.MAX_FILE_SIZE_MO}MB. Add a smaller document.`
      }
      this._onError(msg);
    } else {
      this.uploadFile(list[0]);
    }
  };

  /**
   * Open file dialog
   */
  openFileDialog() {
    if (!this.isDisabled) {
      this.getFileInput().click();
    }
  };
}

FileUploadCtrl.$inject = ['config', '$scope', '$timeout', '$log', '$element'];

angular.module('GLA')
  .component('fileUpload', {
    bindings: {
      cls: '@',
      label: '@',
      uploadParams: '<',
      multiple: '<',
      onProgress: '&',
      onComplete: '&',
      onError: '&',
      isDisabled: '<',
      context: '<',
      componentInputId: '<'
    },
    templateUrl: 'scripts/components/fileUpload/fileUpload.html',
    controller: FileUploadCtrl
  });
