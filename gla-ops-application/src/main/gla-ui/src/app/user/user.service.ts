import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {environment} from "../../environments/environment";
import {assign, cloneDeep, keys, merge, pick} from "lodash-es";
import {SessionService} from "../session/session.service";
import {LocalStorageService} from "ngx-webstorage";
import {catchError, map} from "rxjs/operators";
import {Subject, throwError} from "rxjs";
import {User} from "./user";
import {AutoResume, DEFAULT_INTERRUPTSOURCES, Idle} from "@ng-idle/core";
import {Keepalive} from "@ng-idle/keepalive";

const USER_SESSION_ID = 'user';

const SESSION_DEFAULTS = {
  idleDuration: 60*25,
  timeoutDuration: 60*5,
  keepAliveInterval: 60*5
};

@Injectable({
  providedIn: 'root'
})
export class UserService {

  private user = {
    data: {
      loggedOn: false
    } as User
  };

  private loginSubject = new Subject<any>();
  private logoutSubject = new Subject<{user:any, message: string }>();

  constructor(private http: HttpClient,
              private sessionService: SessionService,
              private localStorageService: LocalStorageService,
              private idle: Idle,
              private keepalive: Keepalive) {
  }

  currentUser() {
    if(!this.user.data.SID) {
      let localStorageUser = this.localStorageService.retrieve(USER_SESSION_ID);
      if(localStorageUser){
        this.user.data =  localStorageUser;
      }
    }
    return this.user.data
  }

  hasPermission(permission: string, orgId?: number) {
    let permissions = (this.currentUser() || {}).permissions || [];
    return permissions.some(p => {
      return p == permission || p == `${permission}.*` || (orgId && p == `${permission}.${orgId}`)
    });
  }

  hasPermissionStartingWith(permission: string){
    return (this.currentUser().permissions || []).some(p => {
      return p.indexOf(permission) == 0;
    });
  }

  passwordStrength (password) {
    return this.http.post<string>(`${environment.basePath}/admin/passwordstrength`, password)
  }

  login (username, password) {
    const user = {
      username: username,
      password: password
    }
    return this.http.post<any>(`${environment.basePath}/sessions`, user).pipe(
      map(response => {
        this.user.data.loggedOn = true;
        this.user.data.SID = response.id;
        assign(this.user.data, response.user);
        this.user.data.isAdmin = (this.user.data.primaryRole === 'Admin');
        this.sessionService.clear();
        this.localStorageService.store(USER_SESSION_ID, this.user.data);
        this.setupUserSession();
        this.loginSubject.next(this.user);
        return this.user;
      }),
      catchError(error => {
        return throwError(error);
      })
    );
  }


  logout (logoutMsg?: string) {
    let deleteObservable = this.http.delete(`${environment.basePath}/sessions/_current`).pipe(
      map(response => {
        this.user.data = {
          loggedOn: false
        };
        this.sessionService.clear();
        this.localStorageService.clear(USER_SESSION_ID);
        this.idle.stop();
        this.logoutSubject.next({user:this.user, message: logoutMsg});
        return response;
      }),
      catchError(error => {
        return throwError(error);
      })
    );
    return deleteObservable;
  }

  onLogin(callback){
    return this.loginSubject.subscribe(callback)
  }

  onLogout(callback){
    return this.logoutSubject.subscribe(callback)
  }

  setupUserSession() {
    let user = this.currentUser();
    if (user && user.loggedOn) {
      let sessionConfig = this.getSessionConfig();
      this.idle.setIdle(sessionConfig.idleDuration);
      this.idle.setTimeout(sessionConfig.timeoutDuration);
      this.idle.setInterrupts(DEFAULT_INTERRUPTSOURCES);
      this.idle.setAutoResume(AutoResume.notIdle)
      this.idle.watch();
      this.keepalive.interval(sessionConfig.keepAliveInterval);
      this.keepalive.start();
    }
  }

  getSessionConfig(){
    let userSessionConfig = pick(this.currentUser(), keys(SESSION_DEFAULTS));
    return merge(cloneDeep(SESSION_DEFAULTS), userSessionConfig);
  }

  requestPasswordReset(userEmail:string) {
    return this.http.post<string>(`${environment.basePath}/password-reset-token`, userEmail.toLowerCase());
    // return $http({
    //   url: config.basePath + '/password-reset-token',
    //   method: 'POST',
    //   headers: {
    //     'Content-Type': 'application/json;charset=UTF-8'
    //   },
    //   data: userEmail.toLowerCase()
    // });
  }

  getUserThresholdsByOrgId(orgId){
    return this.http.get(`${environment.basePath}/userThresholds/organisation/${orgId}`);
  }

}
