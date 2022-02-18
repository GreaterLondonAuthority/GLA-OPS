import {Component, Input, OnInit} from '@angular/core';
import {SessionService} from "../session/session.service";

@Component({
  selector: 'gla-banner-message',
  templateUrl: './banner-message.component.html',
  styleUrls: ['./banner-message.component.scss']
})
export class BannerMessageComponent implements OnInit {

  @Input() message: any;
  @Input() canClose: boolean

  constructor(private sessionService: SessionService) { }

  ngOnInit(): void {
  }

  close() {
    console.log('closing...')
    this.sessionService.setBannerMessageState({
      isDimissed: true,
      message: this.message
    });
  }

}
