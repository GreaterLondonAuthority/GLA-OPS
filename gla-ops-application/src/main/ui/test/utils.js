/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

var config = require('./testConfig.js');
var request = require('request').defaults({
  jar: true,
  rejectUnauthorized: false
});
var moment = require('moment');
var _ = require('lodash');

module.exports = {
  cleanText(text) {
    let textWithoutSpecialCharacters = (text || '').trim().replace(/[\r\n]+/g, ' ');
    let textWithSingleWhiteSpace = textWithoutSpecialCharacters.replace(/\s\s+/g, ' ');
    return textWithSingleWhiteSpace;
  },

  intFromString(formattedIntegerStr){
    let value = 0;
    if(formattedIntegerStr){
      value = +(formattedIntegerStr.trim().replace(/,/g, ''));
    }
    return value;
  },

  mockPromise(valueToReturn) {
    return {
      then(callback) {
        callback(valueToReturn);
        return this;
      },
      catch() {
        return this;
      },
      finally() {
        return this;
      }
    }
  },

  mockFailedPromise(valueToReturn) {
    return {
      then: (successCallback, errorCallback) => {
        if (errorCallback) {
          errorCallback(valueToReturn);
        }
      }
    }
  },

  unapproveUser(organisationId, username, callback) {
    api.loginAsAdmin((err) => {
      if (err) {
        callback(err);
      } else {
        api.unapproveUser(organisationId, username, callback);
      }
    });
  },

  resetProject(projectId, projectTitle, callback) {
    api.loginAsAdmin((err) => {
      if (err) {
        callback(err);
      } else {
        api.resetProject(projectId, projectTitle, callback);
      }
    });
  },

  withdrawProject(projectId, callback) {
    // console.log('Withdrawing project: ', projectId);
    api.loginAsAdmin((err) => {
      if (err) {
        callback(err);
      } else {
        api.withdrawProject(projectId, callback);
      }
    });
  },


  projectAction(projectId, status, subStatus, callback) {
    api.loginAsAdmin((err) => {
      if (err) {
        callback(err);
      } else {
        api.projectAction(projectId, status, subStatus,  callback);
      }
    });
  },

  getProjectById(projectId, callback) {
    api.loginAsAdmin((err) => {
      if (err) {
        callback(err);
      } else {
        api.getProjectById(projectId, callback);
      }
    });
  },

  getProjectByTitle(projectTitle, callback) {
    api.loginAsAdmin((err) => {
      if (err) {
        callback(err);
      } else {
        api.getProjectByTitle(projectTitle, callback);
      }
    });
  },

  getOrganisationByName(orgName, callback) {
    api.loginAsAdmin((err) => {
      if (err) {
        callback(err);
      } else {
        api.getOrganisationByName(orgName, callback);
      }
    });
  },

  unlockProject(projectId, callback) {
    api.unlockProject(projectId, callback);
  },

  sortTableColumn(sortType, sortDir, a, b) {
    var left = a;
    var right = b;
    if ('descending' === sortDir) {
      left = b;
      right = a;
    }

    if (sortType === 'numerically') {
      return left - right;
    } else if (sortType === 'chronologically') {

      var leftDate = moment(left, 'MMM D, YYYY HH:mm', true);
      var rightDate = moment(right, 'MMM D, YYYY HH:mm', true);
      if (leftDate > rightDate) return 1;
      else if (leftDate < rightDate) return -1;
      else return 0;

    } else {
      // need to match back end sorting with javascript's
      var newLeft = left.replace(/[\s\-\.]/g, '').toLowerCase();
      var newRight = right.replace(/[\s\-\.]/g, '').toLowerCase();
      return newLeft.localeCompare(newRight);
    }
  },

  userRequestPasswordReset(userId, callback) {
    return api.userRequestPasswordReset(userId, callback);
  },

  /**
   *
   * @param emailId
   * @param callback
   */
  getEmail(emailId, callback) {
    api.loginAsAdmin((err) => {
      if (err) callback(err);
      else {
        return api.getEmail(emailId, callback);
      }
    });
  },

  createProject(user, projectTitle, programmeName, templateName, callback) {
    api.login(user, (err) => {
      if (err) callback(err);
      else {
        return api.createProject(projectTitle, programmeName, templateName, callback);
      }
    });
  },

  cloneProject(projectToClone, newProjectTitle, callback) {
    api.loginAsAdmin((err) => {
      if (err) callback(err);
      else {
        return api.cloneProject(projectToClone, newProjectTitle, callback);
      }
    });
  },
  runPaymentScheduler(date, callback) {
    api.loginAsAdmin((err) => {
      if (err) callback(err);
      else {
        return api.runPaymentScheduler(date, callback);
      }
    });
  },
  transferProject(projectId, organisationId, callback) {
    api.loginAsAdmin((err) => {
      if (err) {
        callback(err);
      } else {
        return api.transferProject(projectId, organisationId, callback);
      }
    });
  },

  addUser(orgId, username, callback) {
    api.loginAsAdmin((err) => {
      if (err) callback(err);
      else {
        return api.addUser(orgId, username, callback);
      }
    });
  },

  deleteProject(projectId, callback) {
    api.loginAsAdmin(err => {
      if (err) callback(err);
      else {
        return api.deleteProject(projectId, callback);
      }
    });
  },

  deleteProgramme(programmeId, callback) {
    api.loginAsAdmin(err => {
    if (err) callback(err);
    else {
        return api.deleteProgramme(programmeId, callback);
      }
    });
  },

  deleteConsortium(consortiumId, callback) {
    api.loginAsAdmin(err => {
      if (err) callback(err);
      else {
        return api.deleteConsortium(consortiumId, callback);
      }
    });
  },

  bulkProjectOperation(operation, ids, callback) {
    api.loginAsAdmin(err => {
    if (err) callback(err);
    else {
        return api.bulkProjectOperation(operation, ids, callback);
      }
    });
  },

  /**
   * Returns a project block id by block type
   */
  getProjectBlockByType(type, project) {
    let camelCase = (s) => {
      return (s || '').toLowerCase().replace(/(\b|-)\w/g, function (m) {
        return m.toUpperCase().replace(/-/, '');
      });
    }

    return _.find(project.projectBlocksSorted, {
      blockType: camelCase(type)
    });
  },

  cucumberTestsStatistics(stats){
    console.log('browser.stats', stats);
    let summary = Object.keys(stats).map(stepId => {
      let times = _.orderBy(stats[stepId], 'time');
      let count = times.length;
      let totalTime = _.sumBy(times, 'time');
      let averageTime = totalTime / count;
      return {
        stepId: stepId,
        count: count,
        averageTime: +averageTime.toFixed(2),
        totalTime: +totalTime.toFixed(2),
        times: times
      }
    });
    console.log('summary by step count:', JSON.stringify(_.orderBy(summary, 'count', 'desc'), null, 2));
    console.log('summary by average time:', JSON.stringify(_.orderBy(summary, 'averageTime', 'desc'), null, 2));
    console.log('summary by total time:', JSON.stringify(_.orderBy(summary, 'totalTime', 'desc'), null, 2));

    let allSteps = [];
    summary.forEach(item =>{
      allSteps = allSteps.concat(item.times);
    });

    console.log('summary all steps:', JSON.stringify(_.orderBy(allSteps, 'time', 'desc'), null, 2));
  },

  getCurrentFinancialYear() {
    let now = moment();
    const fyStart = now.isBefore(moment(4, 'MM')) ? now.subtract(1, 'year') : now;
    const fyEnd = fyStart.clone().add(1, 'year');
    const fy = `${fyStart.format('YYYY')}/${fyEnd.format('YY')}`;
    return fy;
    // return fyStart.format('YYYY');
  },

  isFirstYearOfFinancialYear(date){
    let startYear = +(date.format('YYYY'));
    let april = moment(`${startYear}/04/01`, 'YYYY/MM/DD');
    if(date.isBefore(april)){
      return false;
    }
    return true;
  },
  /**
   * Converts human readable selector like 'Receipts table' to 'receipts-table'
   * @param readableSelector
   * @returns {string}
   */
  selector(readableSelector){
    return readableSelector.trim().toLowerCase().replace(/ /g, '-');
  },

  /**
   * Returns element by id or class
   * @param idOrClass text representing id or class
   * @return {*}
   */
  elementByIdOrClass(idOrClass){
    return element(by.idOrClass(this.selector(idOrClass)));
  },

  updateMilestoneBlock(projectId, milestonesBlock, callback) {
    api.lockBlock(projectId, milestonesBlock.id, (err, lock) => {
      if (err) callback(err);
      else {
        milestonesBlock.lockDetails = lock.lockDetails;
        return api.updateMilestoneBlock(projectId, milestonesBlock,  callback);
      }
    });
  },

  getBlockByName(project, blockName){
    for (let i = 0; i < project.projectBlocksSorted.length; i++) {
      let block = project.projectBlocksSorted[i];
      if (block.blockDisplayName.toLowerCase() === blockName.toLowerCase()) {
        return block;
      }
    }
    return null;
  },

  updateBlockLastModifiedDate(projectId, blockName, date, callback){
    api.getProjectById(projectId, (err, project)=>{
      if(err){
        return callback(err);
      }
      let block = this.getBlockByName(project, blockName);
      api.updateBlockLastModifiedDate(project.id, block.id, date, callback);
    })
  },

  getBlockHistory(projectId, displayOrder, callback) {
    api.loginAsAdmin((err) => {
      if (err) {
        callback(err);
      } else {
        api.getBlockHistory(projectId, displayOrder, callback);
      }
    });
  },

  getBlock(projectId, blockId, callback) {
    api.loginAsAdmin((err) => {
      if (err) {
        callback(err);
      } else {
        api.getBlock(projectId, blockId, callback);
      }
    });
  },

  /**
   * Execute heavy js sync and async operation to test performance
   * @param scenario
   * @param callback
   * @returns {*}
   */
  jsPerformance(scenario) {
    var count = 0;
    var iterations = 500000;
    var start = new Date().getTime();
    var text = '';
    return new Promise((resolve, reject) => {
      for (var i = 0; i < iterations; i++) {
        setTimeout(function () {
          count++;
          if (count == iterations) {
            var end = new Date().getTime();
            console.log('async time:', end - start);
            resolve();
          }
        }, 0);
      }
      console.log('sync time:', new Date().getTime() - start);
    });
  },

  staticFileDownload(fileName) {
    var start = new Date().getTime();
    return new Promise((resolve, reject) => {
      const options = {
        uri: `${config.baseURL}/${fileName}`,
        method: 'GET',
        headers: {
          'Authorization' : `Basic ${new Buffer(config.adminUser.username + ':' + config.adminUser.password).toString('base64')}`
        }
      };
      request(options, (error, response, body) => {
        console.log('Static download time:', new Date().getTime() - start);
        console.log('Static file url:', `${config.baseURL}/${fileName}`);
        console.log('Static file err:', error);
        console.log('Static file status code:', response.statusCode);
        console.log('Static file body:', body);
        resolve();
      });
    });
  },

  api(){
    return api;
  }
};

var api = {
  createProject(projectTitle, programmeName, templateName, callback) {
    if (_.isFunction(templateName)) {
      callback = templateName;
      templateName = null;
    }
    api.getProgramme(programmeName, (err, programme) => {
      if (err) {
        return callback(err);
      }

      let template = templateName ? _.find(programme.templates, {
        name: templateName
      }) : programme.templates[0];

      const options = {
        uri: `${config.baseURL}/api/v1/projects`,
        method: 'POST',
        json: {
          programme: {
            id: programme.id
          },
          template: {
            id: template.id
          },
          title: projectTitle
        }
      };

      request(options, (error, response, projectId) => {
        if (error || response.statusCode !== 200) {
          console.error(error, response.statusCode, projectId);
          callback(new Error('Failed to create project: ' + response.statusCode + ', ' + (error || {}).message));
        } else {
          this.getProjectById(projectId, callback);
        }
      });
    })
  },

  cloneProject(projectToClone, newProjectTitle, callback) {
    const options = {
      uri: `${config.baseURL}/api/v1/projects/clone?existingProjectTitle=${projectToClone}&clonedProjectTitle=${newProjectTitle}`,
      method: 'POST',
      json: true
    };

    request(options, (error, response, body) => {
      if (error || response.statusCode !== 200) {
        console.error(error, response.statusCode, projectToClone, newProjectTitle);
        callback(new Error('Failed to create project: ' + response.statusCode + ', ' + (error || {}).message));
      } else {
        callback(null, body);
      }
    });
  },

  runPaymentScheduler(date, callback) {
    const options = {
      uri: `${config.baseURL}/api/v1/runScheduledPayments/onDate?date=${date}`,
      method: 'PUT',
      json: true
    };

    request(options, (error, response, body) => {
      if (error || response.statusCode !== 200) {
        console.error(error, response.statusCode, date);
        callback(new Error('Failed to run payment scheduler: ' + response.statusCode + ', ' + (error || {}).message));
      } else {
        callback(null, body);
      }
    });
  },

  transferProject(projectId, organisationId, callback) {
    const options = {
      uri: `${config.baseURL}/api/v1/projects/transferTestProject/${organisationId}`,
      method: 'PUT',
      json: [projectId],
    };

    request(options, (error, response, body) => {
      if (error || response.statusCode !== 200) {
        console.error(error, response.statusCode, projectId);
        callback(new Error('Failed to transfer project: ' + response.statusCode + ', ' + (error || {}).message));
      } else {
        callback(null, body);
      }
    });
  },

  loginAsAdmin: (callback) => {
    const options = {
      uri: `${config.baseURL}/api/v1/sessions`,
      method: 'POST',
      json: {
        username: config.adminUser.username,
        password: config.adminUser.password
      }
    };

    request(options, (error, response, body) => {
      if (error || response.statusCode !== 200) {
        console.error(error, response.statusCode, body);
        callback(new Error('Failed to login: ' + response.statusCode + ', ' + (error || {}).message));
      } else {
        callback();
      }
    });
  },

  login(user, callback) {
    const options = {
      uri: `${config.baseURL}/api/v1/sessions`,
      method: 'POST',
      json: {
        username: user.username,
        password: user.password
      }
    };

    request(options, (error, response, body) => {
      if (error || response.statusCode !== 200) {
        console.error(error, response.statusCode, body);
        callback(new Error('Failed to login: ' + response.statusCode + ', ' + (error || {}).message));
      } else {
        callback();
      }
    });
  },

  unapproveUser(organisationId, username, callback) {
    const options = {
      uri: `${config.baseURL}/api/v1/organisations/${organisationId}/users/${username}/approved?approved=false`,
      method: 'PUT'
    };
    request(options, (error, response, body) => {
      if (error || response.statusCode !== 200) {
        console.error(error, response.statusCode, body);
        callback(new Error('Failed to login'));
      } else {
        callback();
      }
    });
  },

  //We need title because otherwise it tries to set it to null and gives an exception
  resetProject(projectId, projectTitle, callback) {
    const options = {
      uri: `${config.baseURL}/api/v1/projects/${projectId}`,
      method: 'PUT',
      json: {
        title: projectTitle
      }
    };
    request(options, (error, response, body) => {
      if (error || response.statusCode !== 200) {
        console.error(error, response.statusCode, body);
        callback(new Error(`Failed to reset project: ${projectId} : ${projectTitle}`));
      } else {
        callback();
      }
    });
  },

  withdrawProject(projectId, callback) {
    const options = {
      uri: `${config.baseURL}/api/v1/projects/${projectId}/status`,
      method: 'PUT',
      json: {
        status: 'Draft'
      }
    };
    request(options, (error, response, body) => {
      if (error || response.statusCode !== 200) {
        if (response.statusCode === 400) {
          console.warn(`Project already withdrawn. Failed to withdraw project: ${projectId}`, error, response.statusCode, body);
          callback();
        } else {
          console.error(error, response.statusCode, body);
          callback(new Error(`Failed to withdraw project: ${projectId}`));
        }
      } else {
        callback();
      }
    });
  },

  projectAction(projectId, status, subStatus, callback) {
    let options;
    //Special case. State model doesn't allow such transition
    if (status === 'deactivate') {
      options = {
        uri: `${config.baseURL}/api/v1/projects/${projectId}/${status.toLowerCase()}`,
        method: 'PUT',
        body: 'IT_' + new Date().getTime()
      };
    }
    else {
      options = {
        uri: `${config.baseURL}/api/v1/projects/${projectId}/status`,
        method: 'PUT',
        json: {
          status: status,
          subStatus: subStatus,
          comments: 'IT_' + new Date().getTime()
        }
      };
    }

    request(options, (error, response, body) => {
      if (error || response.statusCode !== 200) {
        if (response.statusCode === 400) {
          console.warn(`Unexpected project status for action '${status}'. Failed to ${status} project: ${projectId}`, error, response.statusCode, body);
          callback();
        } else {
          console.error(error, response.statusCode, body);
          callback(new Error(`Failed to ${status} project: ${projectId}`));
        }
      } else {
        callback();
      }
    });
  },

  unlockProject(projectId, callback) {
    const options = {
      uri: `${config.baseURL}/api/v1/projects/${projectId}/removeLocks`,
      method: 'DELETE',
      headers: {
        'Authorization' : `Basic ${new Buffer(config.adminUser.username + ':' + config.adminUser.password).toString('base64')}`
      }
    };
    request(options, (error, response, body) => {
      // console.log('unlocking....', 'err:', error,'status:', response.statusCode, 'body:', body, 'projectId', projectId);
      if (error || response.statusCode !== 200) {
        console.error(error, response.statusCode, body);
        callback(new Error(`Failed to unlock project: ${projectId}`));
      } else {
        callback();
      }
    });
  },

  lockBlock(projectId, blockId, callback){
    const options = {
      uri: `${config.baseURL}/api/v1/projects/${projectId}/lock/${blockId}`,
      method: 'GET',
      json: true,
      headers: {
        'Authorization' : `Basic ${new Buffer(config.adminUser.username + ':' + config.adminUser.password).toString('base64')}`
      }
    };
    request(options, (error, response, body) => {
      if (error || response.statusCode !== 200 || !body.lockRequestSuccessful) {
        console.error(error, response.statusCode, body);
        callback(new Error(`Failed to lock block: ${projectId}`));
      } else {
        console.log('lock blody', body);
        callback(null, body);
      }
    });
  },

  addUser(organisationId, username, callback) {
    const options = {
      uri: `${config.baseURL}/api/v1/organisations/${organisationId}/users/${username}`,
      method: 'PUT'
    };
    request(options, (error, response, body) => {
      if (error || response.statusCode !== 200) {
        console.error(error, response.statusCode, body);
        callback(new Error(`Failed to add user ${username} to organisation with id: ${organisationId}`));
      } else {
        callback();
      }
    });
  },

  userRequestPasswordReset(username, callback) {
    const options = {
      uri: `${config.baseURL}/api/v1/password-reset-token`,
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: username
    };
    request(options, (error, response, body) => {
      if (error || (response.statusCode !== 200 && response.statusCode !== 202)) {
        console.error(error, response.statusCode, body);
        callback(new Error(`Failed to request password reset: ${username}`));
      } else {
        callback(null, JSON.parse(body).emailId);
      }
    });
  },

  getEmail(emailId, callback) {
    const options = {
      uri: `${config.baseURL}/api/v1/emails/${emailId}`,
      method: 'GET'
    };
    request(options, (error, response, body) => {
      if (error || response.statusCode !== 200) {
        console.error(error, response.statusCode, body);
        callback(new Error(`Failed to retrieve email: ${emailId}`));
      } else {
        callback(JSON.parse(body));
      }
    });
  },

  getProjectById(projectId, callback) {
    var options = {
      uri: `${config.baseURL}/api/v1/projects/${projectId}`,
      method: 'GET',
      json: true
    };

    request(options, (error, response, body) => {
      if (error || response.statusCode !== 200) {
        console.error(error, response.statusCode, body);
        callback(new Error(`Failed to get a project: ${projectId}`));
      } else {
        if (!body) {
          return callback(new Error(`${body.length} projects found with ID: ${projectId}`));
        } else {
          callback(null, body);
        }
      }
    });
  },

  getProjectIdByTitle(projectTitle, callback) {
    var options = {
      uri: `${config.baseURL}/api/v1/projects?project=${projectTitle}`,
      method: 'GET',
      json: true
    };

    request(options, (error, response, body) => {
      if (error || response.statusCode !== 200) {
        console.error(error, response.statusCode, body);
        callback(new Error(`Failed to get a project: ${projectTitle}`));
      } else {
        if (body.content.length === 0) {
          return callback(new Error(`${body.content.length} projects found with title: ${projectTitle}`));
        }
        var projectId;
        if(body.content.length === 1) {
          projectId = body.content[0].id;
        } else {
          for (var i = 0; i < body.content.length; i++) {
            if(body.content[i].title === projectTitle){
              projectId = body.content[i].id;
            }
          }
          if(!projectId){
            return callback(new Error(`${body.content.length} projects found with title: ${projectTitle}`));
          }
        }
        callback(null, projectId);
      }
    });
  },

  getProjectByTitle(projectTitle, callback) {
    this.getProjectIdByTitle(projectTitle, (err, projectId) => {
      if (err) {
        callback(err);
      } else {
        this.getProjectById(projectId, callback);
      }
    });
  },



  deleteProgramme(programmeId, callback) {
    const options = {
      uri: `${config.baseURL}/api/v1/programmes/${programmeId}`,
      method: 'DELETE'
    };
    request(options, (error, response, body) => {
      if (error || response.statusCode !== 200) {
      console.error(error, response.statusCode, body);
      callback(new Error(`Failed to delete programme: ${programmeId}`));
    } else {
    console.log(`Successfully deleted programme: ${programmeId}`);
      callback();
    }
  });
  },

  // projectBulkOperation: (ids, operation) => {
  //   return $http({
  //     url: `${urlPrefix}/projects/bulkOperation`,
  //     method: 'PUT',
  //     data: {
  //       operation: operation,
  //       projects: ids
  //     }
  //   });
  // },
  bulkProjectOperation(operation, ids, callback) {
    console.log('-----------\nbulkProjectOperation:',operation,ids);
    const options = {
      uri: `${config.baseURL}/api/v1/projects/bulkOperation`,
      method: 'PUT',
      json: {
        operation: operation,
        projects: ids
      }
    };
    console.log('body:',JSON.stringify({
      operation: operation,
      projects: ids
    }));
    request(options, (error, response, body) => {
      if (error || response.statusCode !== 200) {
      console.error(error, response.statusCode, body);
      callback(new Error(`Failed to do bulk operation operation: ${operation}, ids: ${ids}`));
    } else {
      console.log(`Successfully to do bulk operation operation: ${operation}, ids: ${ids}`);
      callback();
    }
  });
  },

  deleteProject(projectId, callback) {
    const options = {
      uri: `${config.baseURL}/api/v1/projects/${projectId}`,
      method: 'DELETE'
    };
    request(options, (error, response, body) => {
      if (error || response.statusCode !== 200) {
        console.error(error, response.statusCode, body);
        callback(new Error(`Failed to delete project: ${projectId}`));
      } else {
        console.log(`Successfully deleted project: ${projectId}`);
        callback();
      }
    });
  },

  deleteConsortium(consortiumId, callback) {
    const options = {
      uri: `${config.baseURL}/api/v1/organisationGroups/${consortiumId}`,
      method: 'DELETE'
    };
    request(options, (error, response, body) => {
      if (error || response.statusCode !== 200) {
        console.error(error, response.statusCode, body);
        callback(new Error(`Failed to delete consortium: ${consortiumId}`));
      } else {
        callback();
      }
    });
  },

  getProgramme(programmeName, callback) {
    // new line to make sure the file is updated
    var options = {
      uri: `${config.baseURL}/api/v1/programmes?page=0&size=1000`,
      method: 'GET',
      json: true
    };

    request(options, (error, response, programmes) => {
      if (error || response.statusCode !== 200) {
        console.error(error, response.statusCode, programmes);
        callback(new Error(`Failed to get programmes`));
      } else {
        var programme = _.find(programmes.content, {
          name: programmeName
        });
        if (programme) {
          return callback(null, programme);
        } else {
          return callback(new Error(`Programme with name '${programmeName}' not found`));
        }
      }
    });
  },

  updateMilestoneBlock(projectId, milestonesBlock, callback){
    const options = {
      uri: `${config.baseURL}/api/v1/projects/${projectId}/milestones`,
      method: 'PUT',
      json: milestonesBlock
    };

    request(options, (error, response, body) => {
      if (error || response.statusCode !== 200) {
        callback(new Error(`Failed to update milestone for project: ${projectId}`));
      } else {
        callback();
      }
    });
  },

  updateBlockLastModifiedDate(projectId, blockId, date, callback){
    const options = {
      uri: `${config.baseURL}/api/v1/projects/${projectId}/blocks/${blockId}/lastModified`,
      method: 'PUT',
      body: date.format('YYYY-MM-DD')
    };

    request(options, (error, response, body) => {
      if (error || response.statusCode !== 200) {
        callback(new Error(`Failed to update the block's lastModified property: ${projectId}, ${blockId}`));
      } else {
        callback();
      }
    });
  },

  getBlock(projectId, blockId, callback){
    const options = {
      uri: `${config.baseURL}/api/v1/projects/${projectId}/${blockId}`,
      method: 'GET',
      json: true
    };

    request(options, (error, response, body) => {
      if (error || response.statusCode !== 200) {
        callback(new Error(`Failed to get the block's: ${projectId}, ${blockId}`));
      } else {
        callback(null, body);
      }
    });
  },

  getBlockHistory(projectId, displayOrder, callback){
    var options = {
      uri: `${config.baseURL}/api/v1/projects/${projectId}/displayOrder/${displayOrder}/history`,
      method: 'GET',
      json: true
    };

    request(options, (error, response, body) => {
      if (error || response.statusCode !== 200) {
        console.error(error, response.statusCode, body);
        callback(new Error(`Failed to get a project: ${projectId}`));
      } else {
          callback(null, body);
      }
    });
  },


  getOrganisationByName(orgName, callback) {

    var options = {
      uri: `${config.baseURL}/api/v1/organisations/page?searchText=${orgName}`,
      method: 'GET',
      json: true
    };

    request(options, (error, response, body) => {
      if (error || response.statusCode !== 200) {
        console.error(error, response.statusCode, body);
        callback(new Error(`Failed to get a organisation: ${orgName}`));
      } else {
        const orgs = body.content || [];
        if (orgs.length === 0) {
          return callback(new Error(`${orgs.length} organisations found with title: ${orgName}`));
        }
        var org;
        if(orgs.length === 1) {
          org = orgs[0];
        } else {
          for (var i = 0; i < orgs.length; i++) {
            if(orgs[i].name === orgName){
              org = orgs[i];
            }
          }
          if(!org){
            return callback(new Error(`${orgs.length} organisations found with title: ${orgName}`));
          }
        }
        this.getOrganisationById(org.id, callback);
      }
    });
  },

  getOrganisationById(orgId, callback) {

    var options = {
      uri: `${config.baseURL}/api/v1/organisations/${orgId}`,
      method: 'GET',
      json: true
    };

    request(options, (error, response, body) => {
      if (error || response.statusCode !== 200) {
        callback(new Error(`Failed to get the organization: ${orgId}`));
      } else {
        callback(null, body);
      }
    });
  }
};
