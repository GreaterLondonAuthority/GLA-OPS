/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */


const config = {
  lastLoginConfig: false,
  adminUser: {
    isAdmin: true,
    username: 'test.admin@gla.com',
    password: process.env.ADMIN_PASSWORD
  },

  adminUser2: {
    isAdmin: true,
    username: 'user.alpha@gla.org',
    password: process.env.ADMIN_PASSWORD
  },

  //This user is always approved. Don't change status in tests
  rpUserApproved: {
    username: 'testapproved@gla.com',
    password: process.env.ADMIN_PASSWORD,
    firstName: 'Test',
    lastName: 'Approved',
    providerNumber: 'L4241'
  },

  //This user is never approved. Don't change status in tests
  rpUserUnapproved: {
    username: 'testunapproved@gla.com',
    password: process.env.ADMIN_PASSWORD,
    firstName: 'Test',
    lastName: 'Unapproved',
    providerNumber: 'L4241'
  },

  //This user is changed in tests from unapproved to approved. It is reverted after tests back to unapproved
  rpUserForApproval: {
    username: 'unapprovedgla@gmail.com',
    password: process.env.ADMIN_PASSWORD,
    firstName: 'Test',
    lastName: 'Unapproved',
    organisationId: 9999,
    providerNumber: 'L4241'
  },

  //This user is removed in tests. It is reverted after tests back to unapproved
  rpUserForRemoval: {
    username: 'testremoveuser@gla.com',
    password: process.env.ADMIN_PASSWORD,
    organisationId: 9999
  },

  orgAdminUser: {
    username: 'testorgadmin@gla.com',
    password: process.env.ADMIN_PASSWORD,
    firstName: 'Org',
    lastName: 'Admin',
    providerNumber: 'FAKE1'
  },

  withoutRoleUser: {
    username: 'without.role@gla.com',
    password: process.env.ADMIN_PASSWORD,
    firstName: 'Without',
    lastName: 'Role',
    providerNumber: 'FAKE1'
  },

  userWithMultipleOrg: {
    username: 'multi.org@gla.com',
    password: process.env.ADMIN_PASSWORD,
    firstName: 'Multi',
    lastName: 'Org',
    orgA: {
      providerNumber: 'FAKE1',
      organisationId: 9999,
      organisationName: 'Test Registered Partner'
    },
    orgB: {
      providerNumber: 'FAKE2',
      organisationId: 9998,
      organisationName: 'Test Registered Partner 2'
    }
  },

  rpUserApprovedOrg2: {
    username: 'testapproved@testorg2.com',
    password: process.env.ADMIN_PASSWORD
  },

  projectManagerUser: {
    username: 'project.manager@gla.com',
    password: process.env.ADMIN_PASSWORD
  },

  readonlyUser: {
    username: 'testreadonly@gla.com',
    password: process.env.ADMIN_PASSWORD
  },

  readonlyGlaUser: {
    username: 'readonly.gla@gla.com',
    password: process.env.ADMIN_PASSWORD
  },

  progAdmin: {
    username: 'programme.admin@gla.com',
    password: process.env.ADMIN_PASSWORD
  },

  financeUser: {
    username: 'finance.gla@gla.com',
    password: process.env.ADMIN_PASSWORD
  },

  seniorProjectManagerUser: {
    username: 'senior.pm@gla.com',
    password: process.env.ADMIN_PASSWORD
  },

  lesserSeniorProjectManagerUser: {
    username: 'less_senior_pm@gla.com',
    password: process.env.ADMIN_PASSWORD
  },

  multiOrgSpmUser: {
    username: 'spm.multi.managing.orgs@gla.com',
    password: process.env.ADMIN_PASSWORD
  },

  multiOrgPmUser: {
    username: 'pm.multi.managing.orgs@gla.com',
    password: process.env.ADMIN_PASSWORD
  },

  expiredPasswordUser: {
    username: 'expired_pw@gla.ops',
    password: process.env.ADMIN_PASSWORD
  },

  expiredPasswordUserMixedCase: {
    username: 'exPired_Pw@gla.ops',
    password: process.env.ADMIN_PASSWORD
  },

  userWithoutConsortium: {
    username: 'testapproved@testorg4.com',
    password: process.env.ADMIN_PASSWORD
  },

  cultureSPM: {
    username: 'culture.spm@gla.com',
    password: process.env.ADMIN_PASSWORD
  },

  regenAdmin: {
    username: 'regen.admin@gla.com',
    password: process.env.ADMIN_PASSWORD
  },

  cultureAdmin: {
    username: 'culture.admin@gla.com',
    password: process.env.ADMIN_PASSWORD
  },

  hnlAdmin: {
    username: 'hnl.admin@gla.com',
    password: process.env.ADMIN_PASSWORD
  },

  regenSPM: {
    username: 'regen.spm@gla.com',
    password: process.env.ADMIN_PASSWORD
  },

  regenPM: {
    username: 'regen.pm@gla.com',
    password: process.env.ADMIN_PASSWORD
  },

  regenRP: {
    username: 'regen.rp@gla.com',
    password: process.env.ADMIN_PASSWORD
  },

  businessAllocationsRP: {
    username: 'jack@jackspanners.com',
    password: process.env.ADMIN_PASSWORD
  },

  businessNoAllocationsRP: {
    username: 'other.rp@gla.com',
    password: process.env.ADMIN_PASSWORD
  },

  boroughRP: {
    username: 'borough.rp@gla.com',
    password: process.env.ADMIN_PASSWORD
  },

  projectEditor: {
    username: 'project.editor@gla.com',
    password: process.env.ADMIN_PASSWORD
  },

  singleOrgProfileCreator: {
    username: 'org.profile.creator@gla.ops',
    password: process.env.ADMIN_PASSWORD
  },

   techAdminUser: {
      username: 'tech.support@gla.ops',
      password: process.env.ADMIN_PASSWORD,
      firstName: 'Tech',
      lastName: 'Admin'
    },

  techOrgAdminUser: {
    username: 'tech.orgadmin@gla.ops',
    password: process.env.ADMIN_PASSWORD,
    firstName: 'Tech',
    lastName: 'OrgAdmin'
  },

  skillsRP: {
    username: 'skillsapproved@gla.com',
    password: process.env.ADMIN_PASSWORD,
    firstName: 'Skills',
    lastName: 'Approved'
  },
  skills2RP: {
    username: 'skills2approved@gla.com',
    password: process.env.ADMIN_PASSWORD,
    firstName: 'Skills2',
    lastName: 'Approved'
  },
  skillsAdmin: {
    username: 'skills.admin@gla.com',
    password: process.env.ADMIN_PASSWORD,
    firstName: 'Skills',
    lastName: 'Admin'
  },
  skillsSPM: {
    username: 'skills.spm@gla.com',
    password: process.env.ADMIN_PASSWORD,
    firstName: 'Skills',
    lastName: 'SPM'
  },
  mopacRP: {
    username: 'mopac.rp@gla.com',
    password: process.env.ADMIN_PASSWORD
  },
  mopacAdmin: {
    username: 'mopac.admin@gla.com',
    password: process.env.ADMIN_PASSWORD
  },
  mopacPM: {
    username: 'mopac.pm@gla.com',
    password: process.env.ADMIN_PASSWORD
  },
  mopacSPM: {
    username: 'mopac.spm@gla.com',
    password: process.env.ADMIN_PASSWORD
  },
  multiRoleRP: {
    username: 'multi_role_rp_org@gla.org',
    password: process.env.ADMIN_PASSWORD
  },
  multiRoleGLA: {
    username: 'multi_role_org@gla.org',
    password: process.env.ADMIN_PASSWORD
  },
  northWest: {
    username: 'north.west@gla.com',
    password: process.env.ADMIN_PASSWORD
  },
  northEast: {
    username: 'north.east@gla.com',
    password: process.env.ADMIN_PASSWORD
  },
  externalRequestingTeamAccessUser: {
    username: 'external.requesting.team.access@gla.ops',
    password: process.env.ADMIN_PASSWORD
  },

  //This user is always approved. Don't change status in tests
  internalBlockEditor: {
    username: 'internalblock.editor@gla.com',
    password: process.env.ADMIN_PASSWORD,
    firstName: 'Internal Block',
    lastName: 'Editor',
    organisationId: '10000'
  },

  browserSize: {
    lg: {width: 1200, height: 768},
    md: {width: 992, height: 768},
    sm: {width: 768, height: 768},
    xs: {width: 767, height: 768},
    xxs: {width: 400, height: 768}
  },

  baseURL: process.env.E2E_BASE_URL || 'http://ops-dev.london.gov.uk',

  cucumberFilterTags(extraTags) {
    let tags = [];
    if (this.isLocalAPI()) {
      tags.push('~@ignore');
      tags.push('~@skipLocally');
    }
    return tags.concat(extraTags || []);
  },

  isLocalAPI() {
    return process.env.E2E_BASE_URL && process.env.E2E_BASE_URL.indexOf('localhost') > -1
      && process.env.API_URL && process.env.API_URL.indexOf('localhost') > -1;
  }
};

module.exports = config;
