import {Component, HostListener, OnInit, ViewEncapsulation} from '@angular/core';
import {SessionService} from "../session/session.service";
import {MetadataService} from "../metadata/metadata.service";
import {UserService} from "../user/user.service";
import {ConfigurationService} from "../configuration/configuration.service";
// import $ from 'jquery';
declare var $: any;


const HEADER_HEIGHT = 125;

@Component({
  selector: 'gla-top-bar',
  templateUrl: './top-bar.component.html',
  styleUrls: ['./top-bar.component.scss'],
  encapsulation: ViewEncapsulation.None
})
export class TopBarComponent implements OnInit {

  mobileMenuExpanded = false;
  canDismissBanner = false;
  systemOutageMessage: { text:string};
  user: any;
  stickyHeader = false;

  constructor(private sessionService: SessionService,
              private metadataService: MetadataService,
              private userService: UserService,
              private configurationService: ConfigurationService) { }

  ngOnInit(): void {
    this.configurationService.systemOutageMessage().subscribe((response) => {
      this.systemOutageMessage = {text: response};
    });

    this.metadataService.onMetadataChange((metadata) => {
      if(!metadata.loggedOut){
        this.systemOutageMessage = {text: metadata.systemOutageMessage};
        this.canDismissBanner = true;
      } else {
        this.canDismissBanner = false;
      }
    });

    this.user = this.userService.currentUser();

    this.userService.onLogin(() => {
      this.user = this.userService.currentUser();
    });

    this.userService.onLogout( () => {
      this.user = this.userService.currentUser();
    });

    const ctrl = this;

    //Show on hover for desktop but click on mobile
    $('body').on('mouseover', '.navbar .dropdown', null, function(){
      if (!ctrl.isMobile()) {
        $(this).addClass('open')
        ctrl.ariaExpanded($(this), true);
      }
    });

    $('body').on('mouseleave', '.navbar .dropdown', null, function(){
      if (!ctrl.isMobile()) {
        $(this).removeClass('open')
        ctrl.ariaExpanded($(this), false);
      }
    });

    // Close other open dropdown menus once we tab to next menu
    $('body').on('focus', '.navbar a[data-toggle]', null, function(){
      if (!ctrl.isMobile()) {
        let previousExpandedMenu = $('.navbar .dropdown.open .dropdown-toggle').not(this);
        console.log('previousExpandedMenu', previousExpandedMenu.text())
        previousExpandedMenu.dropdown('toggle');
      }
    });

    // Close expanded menu item when we tab outside menu
    $('body').on('focus', '.gla-main-section, .scroll-to-top-link a', null, function(){
      if (!ctrl.isMobile()) {
        $('.navbar .dropdown.open .dropdown-toggle').dropdown('toggle');
      }
    });

    $('body').on('click', '.navbar-collapse a:not(.dropdown-toggle)', null, function (e) {
      if (ctrl.isMobile()) {
        let navMenu =  $('.navbar-collapse');
        navMenu.addClass('no-transition');
        navMenu.collapse('hide');
        setTimeout(()=>{
          navMenu.removeClass('no-transition');
        }, 0);
      }
    });
  }

  shouldShowBanner() {
    let state = this.sessionService.getBannerMessageState();
    let show = false;
    if(this.systemOutageMessage && this.systemOutageMessage.text){
      // if same message as before
      if(state && state.message.text == this.systemOutageMessage.text){
        // if already dismissed
        show = !state.isDimissed;
      } else {
        show = true;
      }
    }
    return show;
  }

  @HostListener('window:scroll', ['$event'])
  onWindowScroll(e) {
    if (window.pageYOffset > HEADER_HEIGHT) {
      this.setStickyHeader(true);
    } else {
      this.setStickyHeader(false);
    }
    this.mobileMenuExpanded = this.isMobileMenuExpanded();
  }

  setStickyHeader(stickyHeader: boolean){
    if (this.stickyHeader !== stickyHeader) {
      this.stickyHeader = stickyHeader;
      this.onStickyHeaderChange(stickyHeader)
    }
  }

  onStickyHeaderChange(isFixed){
    this.enableStickyButtons(isFixed);
  }

  enableStickyButtons(enabled: boolean){
    if(enabled){
      let stickyBtn = $('.btn-sticky');
      if(stickyBtn && stickyBtn.length) {
        stickyBtn.css({top: stickyBtn.offset().top - 125});
        stickyBtn.css({right: $(window).width() - (stickyBtn.offset().left + stickyBtn.outerWidth())});
        stickyBtn.addClass('floating-btn');
      }
    } else {
      $('.btn-sticky').removeClass('floating-btn');
    }
  }

  isMobile(){
    return $('.navbar-toggle').is(':visible');
  }

  isMobileMenuExpanded(){
    return $('.navbar-toggle[aria-expanded=true]').is(':visible');
  }

  ariaExpanded($li, expanded){
    $li.find('.dropdown-toggle').attr('aria-expanded', expanded)
  }
}
