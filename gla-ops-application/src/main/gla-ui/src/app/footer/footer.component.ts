import {Component, Input, OnInit} from '@angular/core';
import {ConfigurationService} from "../configuration/configuration.service";

@Component({
  selector: 'gla-footer',
  templateUrl: './footer.component.html',
  styleUrls: ['./footer.component.scss']
})
export class FooterComponent implements OnInit {

  aboutUrl: String
  accessibilityUrl: String
  environment: String
  privacyUrl: String

  constructor(private configurationService: ConfigurationService) { }

  ngOnInit(): void {
    this.configurationService.getConfig().subscribe(config => {
        if (config && config['system-environment']) {
          this.environment = config['system-environment'];
          this.aboutUrl = config['about-url'];
          this.accessibilityUrl = config['accessibility-url'];
          this.privacyUrl = config['privacy-policy-url'];
        }
    });
  }
}
