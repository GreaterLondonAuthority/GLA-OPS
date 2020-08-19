/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */



const SESSION_DEFAULTS = {
  idleDuration: 60*25,
  timeoutDuration: 60*5,
  keepAliveInterval: 60*5
};

UserService.$inject = ['$http', 'config', '$rootScope', '$sessionStorage', '$state', 'SessionService', '$cookies', '$localStorage', 'Idle', 'Keepalive'];


function UserService($http, config, $rootScope, $sessionStorage, $state, SessionService, $cookies, $localStorage, Idle, Keepalive) {

  let user = {
    data: {
      loggedOn: false
    }
  };


  let userService = {

    searchOptions() {
      let res = [
        {
          name: 'username',
          description: 'User name or email',
          hint: 'Enter user name or email',
          maxLength: '50'
        }];
      let organisations = this.currentUser().organisations;
      if(organisations.length > 1 || _.find(organisations, {isManagingOrganisation: true})){

        res.push({
          name: 'organisation',
          description: 'Org name or ID',
          hint: 'Enter Org name or ID',
          maxLength: '50'
        });
      }
      return res;
    },
    getRegistrationStatusOptions() {
      return [
        {
          checkedClass: 'pending',
          ariaLabel: 'Pending',
          name: 'pending',
          model: undefined,
          label: 'Pending',
          key: 'Pending',
        }, {
          checkedClass: 'approved',
          ariaLabel: 'Approved',
          name: 'approved',
          model: undefined,
          label: 'Approved',
          key: 'Approved',
        }
      ];
    },

    getUserStatusOptions() {
      return [
        {
          checkedClass: 'Active',
          ariaLabel: 'Active',
          name: 'active',
          model: undefined,
          label: 'Active',
          key: 'true',
        }, {
          checkedClass: 'Inactive',
          ariaLabel: 'Inactive',
          name: 'inactive',
          model: undefined,
          label: 'Inactive',
          key: 'false',
        }
      ];
    },
    // GLA staff can select all system roles from the dropdown filter

    // External non GLA roles can only see and select 'Project Manager' and 'Org Admin', the GLA roles do not display within the filter list


    getUserRoleOptions(isGLA) {
      let res = [];

      if(isGLA){

        res = _.concat(res, [
          {
            checkedClass: 'opsAdmin',
            ariaLabel: 'OPS Admin',
            name: 'opsAdmin',
            model: undefined,
            label: 'OPS Admin',
            key: 'ROLE_OPS_ADMIN',
          }, {
            checkedClass: 'glaOrgAdmin',
            ariaLabel: 'GLA Organisation Admin',
            name: 'glaOrgAdmin',
            model: undefined,
            label: 'GLA Organisation Admin',
            key: 'ROLE_GLA_ORG_ADMIN',
          }, {
            checkedClass: 'seniorProjectManager',
            ariaLabel: 'Senior Project Manager',
            name: 'seniorProjectManager',
            model: undefined,
            label: 'Senior Project Manager',
            key: 'ROLE_GLA_SPM',
          }, {
            checkedClass: 'projectManager',
            ariaLabel: 'Project Manager',
            name: 'projectManager',
            model: undefined,
            label: 'Project Manager',
            key: 'ROLE_GLA_PM',
          }, {
            checkedClass: 'registrationApprover',
            ariaLabel: 'Registration Approver',
            name: 'registrationApprover',
            model: undefined,
            label: 'Registration Approver',
            key: 'ROLE_GLA_REGISTRATION_APPROVER',
          }, {
            checkedClass: 'glaFinance',
            ariaLabel: 'GLA Finance',
            name: 'glaFinance',
            model: undefined,
            label: 'GLA Finance',
            key: 'ROLE_GLA_FINANCE',
          }, {
            checkedClass: 'glaReadOnly',
            ariaLabel: 'GLA Read Only',
            name: 'glaReadOnly',
            model: undefined,
            label: 'GLA Read Only',
            key: 'ROLE_GLA_READ_ONLY',
          }, {
          checkedClass: 'technicalAdmin',
          ariaLabel: 'Technical Admin',
          name: 'technicalAdmin',
          model: undefined,
          label: 'Technical Admin',
          key: 'ROLE_TECH_ADMIN',
        }]);
      }
      res = _.concat(res,
        [{
          checkedClass: 'orgAdmin',
          ariaLabel: 'Organisation Admin',
          name: 'orgAdmin',
          model: undefined,
          label: 'Organisation Admin',
          key: 'ROLE_ORG_ADMIN',
        }, {
          checkedClass: 'projectEditor',
          ariaLabel: 'Project Editor',
          name: 'projectEditor',
          model: undefined,
          label: 'Project Editor',
          key: 'ROLE_PROJECT_EDITOR',
        }, {
          checkedClass: 'projectReader',
          ariaLabel: 'Project Reader',
          name: 'projectReader',
          model: undefined,
          label: 'Project Reader',
          key: 'ROLE_PROJECT_READER',
        }
      ]);
      return res;
    },

    getOrganisationTypesOptions(organisationTypes) {
      let res = [];
      _.forEach(organisationTypes, (type, index) => {
        res.push({
          checkedClass: _.camelCase(type),
          ariaLabel: type,
          name: _.camelCase(type),
          model: undefined,
          label: type,
          key: index,
        })
      });
      return res;
    },

    getSpendAuthorityOptions() {
      return [{
        checkedClass: 'pendingChanges',
        ariaLabel: 'Pending changes',
        name: 'pendingChanges',
        model: undefined,
        label: 'Pending changes',
        key: 'pendingChanges',
      }, {
        checkedClass: 'notSet',
        ariaLabel: 'Not set',
        name: 'notSet',
        model: undefined,
        label: 'Not set',
        key: 'notSet',
      }, {
        checkedClass: 'usersWithSpendAuthority',
        ariaLabel: 'Users with spend authority',
        name: 'usersWithSpendAuthority',
        model: undefined,
        label: 'Users with spend authority',
        key: 'usersWithSpendAuthority',
      }]
    },

    /**
     * Request password reset
     * @param {String} userEmail
     * @returns {Object} promise
     */
    requestPasswordReset (userEmail) {
      return $http({
        url: config.basePath + '/password-reset-token',
        method: 'POST',
        headers: {
          'Content-Type': 'application/json;charset=UTF-8'
        },
        data: userEmail.toLowerCase()
      });
    },

    /**
     * Check the validity of the reset token
     * @param {Number} userId
     * @param {String} token
     * @returns {Object} promise
     */
    checkPasswordResetToken (userId, token) {
      return $http({
        url: config.basePath + '/password-reset-token/' + userId + '/' + token,
        method: 'GET'
      })
    },

    /**
     * Update the user's password
     * @param {String} userEmail - user email
     * @param {Object} data - { id:Integer, token:String, password:String }
     * @returns {Object} promise
     */
    updateResetPassword (userEmail, data) {
      return $http({
        url: config.basePath + '/users/' + userEmail.toLowerCase() + '/password',
        method: 'PUT',
        data: data,
        serialize: false
      });
    },

    /**
     * Add user to a team
     * @param {User[]} users - user emails
     * @param {Number} teamId - team ID
     * @returns {Object} promise
     */
    addUsersToTeam (userEmails, teamId) {
      return $http({
        url: config.basePath + '/users/team/' + teamId,
        method: 'PUT',
        data:userEmails,
        serialize: false
      });
    },

    /**
     * Update the user's primary organisation
     * @param {String} userEmail - user email
     * @param {Object} organisationId - new organisation to make primary
     * @returns {Object} promise
     */
    updatePrimaryOrganisation (userEmail, organisationId, roleName) {
      return $http({
        url: config.basePath + '/users/' + userEmail.toLowerCase() + '/makePrimaryOrganisation/' + organisationId + '/roleName/' + roleName,
        method: 'PUT',
        serialize: false
      });
    },

    /**
     * Retrieve list of roles the user can assign
     * @param  {String} userEmail - user email
     * @return {Object} promise
     */
    retrieveAssignableRoles (userEmail) {
      return $http({
        url: config.basePath + '/assignable-roles',
        method: 'GET'
      })
    },

    /**
     * Update the role for a user
     * @param  {String} userEmail - user email
     * @param  {roleId} roleId - id of role
     * @return {Object} promise
     */
    updateUserRole (userEmail, organisationId, roleId) {
      return $http({
        url: config.basePath + '/users/' + userEmail + '/role',
        method: 'PUT',
        data: {
          name: roleId,
          organisationId: organisationId
        },
        serialize: false
      })
    },

    /**
     * add secondary role for a user
     * @param  {String} userEmail - user email
     * @param  {roleId} roleId - id of role
     * @return {Object} promise
     */
    addAdditionalUserRole (userEmail, organisationId, roleId) {
      return $http({
        url: config.basePath + '/users/' + userEmail + '/role',
        method: 'POST',
        data: {
          name: roleId,
          organisationId: organisationId
        },
        serialize: false
      })
    },

    /**
     * Log in with username and password
     * @param {string} username
     * @param {string} password
     * @returns {Promise}
     */
    login (username, password) {
      return $http({
        url: config.basePath + '/sessions',
        method: 'POST',
        data: {
          username: username,
          password: password
        }
      }).then(this.userLoginHandler);
    },

    /**
     * Log out current user
     * @returns {Promise}
     */
    logout (logoutMsg) {
      var promise = $http({
        url: config.basePath + '/sessions/_current',
        method: 'DELETE'
      });
      userLogoutHandler(logoutMsg);
      return promise;
    },

    currentUser() {
      if(!user.data.SID) {
        // let cookieUser = $cookies.getObject('user');
        let cookieUser = $localStorage.user;// $sessionStorage.user;
        // $window;
        // $localStorage;
        if(cookieUser){
          //On new tab or refresh session is not lost but $sessionStorage is empty so using $cookies instead
          user.data =  cookieUser;
        }
      }
      return user.data
    },


    /**
     * Get current user's organisations.
     * @param permission You can specify a permission to filter them by org specific permission
     * @returns {Array|*}
     */
    currentUserOrganisations (permission) {
      let user = this.currentUser();
      let organisations = user.organisations || [];
      if (permission) {
        if(user.permissions.indexOf(`${permission}.*`) !== -1){
          //ops admin
          return organisations;
        }
        let permissionRegexStr = `^${permission}.\\d+$`;
        permissionRegexStr = permissionRegexStr.replace(/\./g, '\\.');
        let regexp = new RegExp(permissionRegexStr);
        let orgsWithSpecPermissions = user.permissions.reduce((orgIds, p) => {
          if (regexp.test(p)) {
            orgIds.push(+(p.replace(`${permission}.`, '')));
          }
          return orgIds;
        }, []);
        organisations = organisations.filter(org => orgsWithSpecPermissions.indexOf(org.id) > -1)
      }
      return organisations;
    },

    /**
     * Checks if current user has a specified permission
     * @param {string} permission A permission to check
     * @returns {boolean} Returns true if has a permission
     */

    hasPermissionToAuthorise:function(){
      let user = this.currentUser();
      let managingOrgIds = _.map(user.organisations, 'managingOrganisationId');
      let organisationIds = _.map(user.organisations, 'id');
      let res = {};

      _.map(managingOrgIds, (id)=> {
        //console.log(this.hasPermission('payments.authorise',id));
        res[id] = this.hasPermission('payments.authorise',id);
      });
      _.map(organisationIds, (id)=> {
        res[id] = this.hasPermission('payments.authorise',id);
      });
      return res;
    },

    hasPermission:function(permission, orgId){
      let permissions = this.currentUser().permissions || [];
      return permissions.some(p => {
        return p == permission || p == `${permission}.*` || (orgId && p == `${permission}.${orgId}`)
      });
    },

    hasPermissionStartingWith:function(permission){
      return (this.currentUser().permissions || []).some(p => {
        return p.indexOf(permission) == 0;
      });
    },

    isCurrentUserAllowedToAccessSkillsGateway() {
      return $http({
        url: `${config.basePath}/isCurrentUserAllowedToAccessSkillsGateway`,
        method: 'GET'
      });
    },

    passwordStrength (password) {
      return $http({
        url: config.basePath + '/admin/passwordstrength',
        method: 'POST',
        data: password
      })
    },


    registerUser(user){
      return $http({
        url: config.basePath + '/users',
        method: 'POST',
        data: user
      });
    },

    /**
    * Stores current user object for use accross application
    * @param response
    * @returns {*}
    */
    userLoginHandler(response) {
      //Moved from USER.js
      //TODO we don't need user.data
      user.data.loggedOn = true;
      user.data.SID = response.data.id;
      _.assign(user.data, response.data.user);
      user.data.isAdmin = (user.data.primaryRole === 'Admin');
      // $cookies.putObject('user', user.data);

      SessionService.setDoNotShowAgainDeleteNotificationModal(false);
      SessionService.clear();
      // $sessionStorage.user = user.data;
      $localStorage.user = user.data;


      $rootScope.$broadcast('user.login');

      return user;
    },

    checkCurrentUserAccess(entityType, entityId, ignoreErrors) {
      return $http({
        url: config.basePath + `/checkCurrentUserAccess?entityType=${entityType}&entityId=${entityId}`,
        method: 'GET',
        ignoreErrors: ignoreErrors
      })
    },

    getUserProfile(userName) {
      return $http({
        url: `${config.basePath}/users/${userName}/`,
        method: 'GET'
      });
    },

    getUserRoles(data){
      let cfg = {
        params: {
          page: data.page,
          size:50,
          sort:['firstName,asc','lastName,asc','orgName,asc'],
          username: data && data.username,
          organisation: data && data.organisation,
          registrationStatus: data && data.registrationStatus,
          roles: data && data.userRoles,
          orgTypes: data && data.organisationTypes,
          spendAuthority: data && data.spendAuthority

        }
      };

      return $http.get(`${config.basePath}/user-roles`, cfg)//?size=2000&sort=firstName,asc&sort=lastName,asc,&sort=orgName,asc`);
    },

    getUsers(data){
      let cfg = {
        params: {
          page: data.page,
          size:50,
          sort:['username', 'firstName,asc','lastName,asc'],
          username: data && data.username,
          organisation: data && data.organisation,
          registrationStatus: data && data.registrationStatus,
          userStatus: data && data.userStatus,
          roles: data && data.userRoles,
          orgTypes: data && data.organisationTypes,
          spendAuthority: data && data.spendAuthority

        }
      };

      return $http.get(`${config.basePath}/users`, cfg)//?size=2000&sort=firstName,asc&sort=lastName,asc,&sort=orgName,asc`);
    },

    updateUserStatus(username, enabled) {
      return $http.put(`${config.basePath}/users/${username}/status?enabled=${enabled}`);
    },

    getUserThresholds(username){
      return $http.get(`${config.basePath}/userThresholds/${username}/`);
    },

    updateUserThreshold(username, orgId, pendingAmount){
      return $http.put(`${config.basePath}/userThresholds/${username}/organisation/${orgId}/pendingThreshold/`, pendingAmount);
    },

    approveUserThreshold(username, orgId){
      return $http.put(`${config.basePath}/userThresholds/${username}/organisation/${orgId}/approve/`);
    },

    declineUserThreshold(username, orgId){
      return $http.put(`${config.basePath}/userThresholds/${username}/organisation/${orgId}/decline/`);
    },

    getSessionConfig(){
      let userSessionConfig = _.pick(this.currentUser(), _.keys(SESSION_DEFAULTS));
      return _.merge(angular.copy(SESSION_DEFAULTS), userSessionConfig);
    },

    setupUserSession() {
      let user = this.currentUser();
      if (user && user.loggedOn) {
        let sessionConfig = this.getSessionConfig();
        Idle.setIdle(sessionConfig.idleDuration);
        Idle.setTimeout(sessionConfig.timeoutDuration);
        Idle.watch();
        Keepalive.setInterval(sessionConfig.keepAliveInterval);
        Keepalive.start();
      }
    }

  };

  $rootScope.$on('user.login', function() {
    userService.setupUserSession();
  });


  /**
   * Removes current user object
   * @param logoutMsg
   * @returns {*}
   */
  function userLogoutHandler(logoutMsg) {
    //Moved from USER.js
    user.data = {
      loggedOn: false
    };
    SessionService.clear();
    delete $localStorage.user;
    $cookies.remove('user');

    $rootScope.$broadcast('user.logout');
    if($state.current.name !== 'home') {
      $state.go('home', {reasonError: logoutMsg});
    }else{
      $rootScope.showGlobalLoadingMask = false;
    }

    return logoutMsg;
  }

  return userService;

}

angular.module('GLA')
  .service('UserService', UserService);
