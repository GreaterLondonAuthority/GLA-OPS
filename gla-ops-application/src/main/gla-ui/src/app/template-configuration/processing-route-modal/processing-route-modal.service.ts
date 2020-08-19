import {Injectable} from '@angular/core';
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {ProcessingRouteModalComponent} from "./processing-route-modal.component";

@Injectable()
export class ProcessingRouteModalService {

  constructor(private modalService: NgbModal) { }

  show(processingRoute, processingRoutes){
    const modal = this.modalService.open(ProcessingRouteModalComponent)
    modal.componentInstance.processingRoute = processingRoute || {};
    modal.componentInstance.processingRoutes = processingRoutes || [];
    return modal;
  }
}
