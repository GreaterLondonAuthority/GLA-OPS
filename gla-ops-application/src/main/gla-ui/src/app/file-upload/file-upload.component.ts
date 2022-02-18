import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { LoadingMaskService } from '../shared/loading-mask/loading-mask.service';
import {ToastrUtilService} from "../shared/toastr/toastr-util.service";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import { DeleteFileModalComponent } from './delete-file-modal/delete-file-modal.component';
import { ErrorModalComponent } from '../shared/error/error-modal/error-modal.component';

const ONE_MB = 1024 * 1024

@Component({
  selector: 'gla-file-upload',
  templateUrl: './file-upload.component.html',
  styleUrls: ['./file-upload.component.scss']
})
export class FileUploadComponent implements OnInit {
  @Input() attachments: any[] = []
  @Input() readOnly: boolean = false
  @Input() additionalCompletionActions: boolean = false
  @Input() additionalRemovalActions: boolean = false
  @Input() attachmentsTotalSize: number
  @Input() postUrl: String
  @Input() maxCombinedUploadSizeInMb: number
  @Input() maxNumberAttachments: number = 99
  @Input() maxUploadSizeInMbPerAttachment: number
  @Input() uniqueId: number
  @Input() uploadParams: any
  @Input() downloadUrl: any
  @Output() onCompleteActions: EventEmitter<any> = new EventEmitter()
  @Output() onRemovalActions: EventEmitter<any> = new EventEmitter()
  context: any = {}
  componentId: string

  constructor(private loadingMaskService: LoadingMaskService,
    private ngbModal: NgbModal,
    private toastrUtil: ToastrUtilService) { }

  ngOnInit(): void {
    this.componentId = this.uniqueId ? 'file-upload-' + this.uniqueId : 'file-upload-input'
  }

  removeAttachment(file: any) {
    const modal = this.ngbModal.open(DeleteFileModalComponent)
    modal.componentInstance.file = file
    modal.componentInstance.title = 'Delete Attachment'
    modal.componentInstance.message = 'Are you sure you wish to remove this attachment?'

    if (this.additionalRemovalActions) {
      modal.result.then(() => {
        this.onRemovalActions.emit({
          response: file
        })
      });
    } else {
      modal.result.then(() => {
        let index = this.attachments.indexOf(file)
        if (index !== -1) {
          this.attachments.splice(index, 1);
        }
      });
    }
  }

  getRemainingCombinedFileSizeReadable(): String {
    let remaining = this.maxCombinedUploadSizeInMb * ONE_MB - this.attachmentsTotalSize;
    let result = Math.max(0, remaining / ONE_MB).toFixed(1)
    return result
  }

  getRemainingCombinedFileSize(): number {
    return this.maxCombinedUploadSizeInMb * ONE_MB - this.attachmentsTotalSize;
  }

  addButtonDisabled() {
    return this.attachments && this.attachments.length >= this.maxNumberAttachments
  }

  onFileUploadComplete(e: any) {
    var file = e.response;

    if (this.additionalCompletionActions) {
      this.onCompleteActions.emit({
        response: file
      })
    } else {
      this.loadingMaskService.showLoadingMask(false);
      async () => {
        this.attachments.push({
          fileId: file.id,
          fileName: file.fileName,
          fileSize: file.fileSize,
          createdOn: file.createdOn,
          creatorName: file.creatorName,
        });
      }
      this.toastrUtil.success('Added');
    }
  }

  onFileUploadError(e: any) {
    this.loadingMaskService.showLoadingMask(false);
    const modal = this.ngbModal.open(ErrorModalComponent)
    modal.componentInstance.error = e
  }

  onFileUploadProgress(e: any) {
    this.loadingMaskService.showLoadingMask(true);
  }

  openFileDialog() {

  }

  handleFileInput(event: any) {

  }



}
