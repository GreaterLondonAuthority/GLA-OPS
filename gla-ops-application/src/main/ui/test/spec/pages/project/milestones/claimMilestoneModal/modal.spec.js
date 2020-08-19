/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

describe('Milestons: Claim: Modal', () => {
   let config = {
   };

   beforeEach(angular.mock.module('GLA'));

   let ClaimMilestoneModal, $compile, $rootScope, $scope, element, milestone, $componentController, modal, maxClaims;

   beforeEach(inject(function (_$componentController_) {
     $componentController = _$componentController_;
   }));


   beforeEach(inject($injector => {
     ClaimMilestoneModal = $injector.get('ClaimMilestoneModal');
     $rootScope = $injector.get('$rootScope');
     $scope = $rootScope.$new();

    //    milestone: {},
    //    maxClaims: undefined,
    //    readOnly: false,
    //    grantValue: undefined,
    //    monetarySplitTitle: undefined,
    //    zeroGrantRequested: undefined
    milestone = {
      claimStatus: 'Claimed',
      claimedDpf: null,
      claimedExceeded: false,
      claimedGrant: null,
      claimedRcgf: null,
      comparisonId: '3005',
      conditional: false,
      createdBy: 'test.glaops@gmail.com',
      createdOn: '2017-09-11T09:53:58.523+01:00',
      description: null,
      displayOrder: 0,
      externalId: 3005,
      id: 1736,
      manuallyCreated: false,
      milestoneDate: '2017-05-10',
      milestoneStatus: 'ACTUAL',
      modifiedBy: null,
      modifiedOn: null,
      monetary: false,
      monetarySplit: 0,
      monetaryValue: null,
      requirement: 'mandatory',
      summary: 'Land acquired'
    };

    maxClaims = {
      RCGF: undefined,
      DPF: undefined
    };

    }));
   describe('Claim milestone modal default', ()=>{
     afterEach(()=>{
       getModalDialog().remove();
     });
     it('Should show skeleton modal', ()=>{
       modal = ClaimMilestoneModal.show(milestone, {milestoneType: 'MonetarySplit'});
       $rootScope.$digest();
       expect(getModalDialog().text()).toContain('CLAIMED MILESTONE');
       expect(dismissBtn().text()).toContain('Close');
       getModalDialog().remove();
       expect(getModalDialog().text()).not.toContain('CLAIMED MILESTONE');
     });
   });
   describe('Claim milestone modal: non financial milestone', ()=>{
     afterEach(()=>{
       getModalDialog().remove();
     });
     it('Claimable non financial milestone', ()=>{

       milestone.claimStatus = 'Pending';

       modal = ClaimMilestoneModal.show(milestone, {milestoneType: 'MonetarySplit'});
       $rootScope.$digest();
       expect(getModalDialog().text()).toContain('CLAIM MILESTONE');
       expect(getModalDialog().text()).toContain('By claiming the milestone you are confirming the milestone has been completed.');
       expect(claimBtn().text()).toEqual('CLAIM MILESTONE');
       expect(cancelBtn().length).toEqual(0);

       expect(dismissBtn().text()).toContain('Close');
     });
     it('Claimed non financial milestone', ()=>{
       milestone.claimStatus = 'Claimed';

       modal = ClaimMilestoneModal.show(milestone, {milestoneType: 'MonetarySplit'});
       $rootScope.$digest();
       expect(getModalDialog().text()).toContain('CLAIMED MILESTONE');
       expect(getModalDialog().text()).toContain('No grant has been claimed with this milestone.');
       expect(getModalDialog().text()).toContain('Milestone claims must be cancelled before the milestone can be edited.');
       expect(cancelBtn().text()).toEqual('CANCEL CLAIM');
       expect(dismissBtn().text()).toContain('Close');
     });
   });
   describe('Claim milestone modal: Monetary milestone (GLA-11774)', ()=>{
     afterEach(()=>{
       getModalDialog().remove();
     });
     it('Claimable monetary milestone', ()=>{
       milestone.claimStatus = 'Pending';
       milestone.monetary = true;
       milestone.monetarySplit = 50;


       modal = ClaimMilestoneModal.show(milestone, {readOnly:false, grantValue:1000, milestoneType:'MonetarySplit'});
       $rootScope.$digest();
       expect(getModalDialog().text()).toContain('CLAIM MILESTONE');
       expect(getModalDialog().text()).toContain('50% of the project\'s grant claimed with this milestone');
       expect(getModalDialog().text()).toContain('Grant Total: £500');
       expect(getModalDialog().text()).toContain('By claiming the milestone you are confirming the milestone has been completed. Claimed payments will display in the Payments section of GLA OPS once the project changes have been approved.');
       expect(getModalDialog().text()).not.toContain('Grant payment amount £');
       expect(claimBtn().text()).toEqual('CLAIM MILESTONE');
       expect(dismissBtn().text()).toContain('Close');
     });
     it('Claimed monetary milestone', ()=>{
       milestone.claimStatus = 'Claimed';
       milestone.monetary = true;
       milestone.monetarySplit = 50;

       modal = ClaimMilestoneModal.show(milestone, {readOnly:false, grantValue:1000, milestoneType:'MonetarySplit'});
       $rootScope.$digest();
       expect(getModalDialog().text()).toContain('CLAIMED MILESTONE');
       expect(getModalDialog().text()).toContain('50% of the project\'s grant claimed with this milestone');
       expect(getModalDialog().text()).toContain('Grant Total: £500');
       expect(getModalDialog().text()).toContain('Milestone claims must be cancelled before the milestone can be edited.');
       expect(cancelBtn().text()).toEqual('CANCEL CLAIM');
       expect(dismissBtn().text()).toContain('Close');
     });
   });

   describe('Modal in readOnly Mode', ()=>{
     afterEach(()=>{
       getModalDialog().remove();
     });
     it('Shows RCGF amount claimed', ()=>{
       milestone.claimStatus = 'Claimed';
       milestone.claimedRcgf = 1000;
       milestone.claimedDpf = 500;
       maxClaims.RCGF = 1000;
       maxClaims.DPF = 500;
       milestone.monetary = true;
       milestone.monetarySplit = 60;

       modal = ClaimMilestoneModal.show(milestone, {maxClaims:maxClaims, readOnly:true, grantValue:2000, milestoneType:'MonetarySplit'});
       $rootScope.$digest();
       expect(getModalDialog().text()).toContain('CLAIMED MILESTONE');

       expect(getModalDialog().text()).toContain('60% of the project\'s grant claimed with this milestone');
       expect(getModalDialog().text()).toContain('Grant Total: £1,200');

       expect(getModalDialog().text()).toContain('Funding from grant sources at this milestone');
       expect(getModalDialog().text()).toContain('RCGF: £1,000');
       expect(getModalDialog().text()).toContain('Milestone claims must be cancelled before the milestone can be edited.');
       expect(getRCGFInput().length).toEqual(0);

       expect(getModalDialog().text()).not.toContain('Are you applying any funding from other grant sources at this milestone?');
       expect(getModalDialog().text()).not.toContain('Total unclaimed RCGF: £1,000');
       expect(getModalDialog().text()).not.toContain('RCGF payment amount £');

       expect(getModalDialog().text()).toContain('DPF: £500');
       expect(getModalDialog().text()).toContain('Milestone claims must be cancelled before the milestone can be edited.');
       expect(getDPFInput().length).toEqual(0);

       expect(getModalDialog().text()).not.toContain('Are you applying any funding from other grant sources at this milestone?');
       expect(getModalDialog().text()).not.toContain('Total unclaimed DPF: £1,000');
       expect(getModalDialog().text()).not.toContain('DPF payment amount £');

       // No cancel claim button
       expect(cancelBtn().length).toEqual(0)
       expect(claimBtn().length).toEqual(0)
       expect($('input').length).toEqual(0)

       expect(dismissBtn().text()).toContain('Close');
     });
   });
   describe('Modal in zero grant Mode', ()=>{
     afterEach(()=>{
       getModalDialog().remove();
     });
     it('Claim milestone zeroGrantRequested', ()=>{

       milestone.claimStatus = 'Pending';
       maxClaims.RCGF = 0;
       maxClaims.DPF = 0;
       milestone.monetary = true;
       milestone.monetarySplit = 60;

       modal = ClaimMilestoneModal.show(milestone, {maxClaims: maxClaims,  grantValue:2000, zeroGrantRequested:true, milestoneType:'MonetarySplit'});
       $rootScope.$digest();

       expect(getModalDialog().text()).toContain('CLAIM MILESTONE');
       expect(getModalDialog().text()).toContain('By claiming the milestone you are confirming the milestone has been completed.');
       expect(claimBtn().text()).toEqual('CLAIM MILESTONE')
       expect(dismissBtn().text()).toContain('Close');

       expect(getModalDialog().text()).not.toContain('60% of the project\'s grant claimed with this milestone');
       expect(getModalDialog().text()).not.toContain('Grant Total: £1,200');
       expect(getModalDialog().text()).not.toContain('By claiming the milestone you are confirming the milestone has been completed. Claimed payments will display in the Payments section of GLA OPS once the project changes have been approved.');



     });
   });

   describe('Modal in associated project Mode GLA-11823', ()=>{
     afterEach(()=>{
       getModalDialog().remove();
     });
     it('Claim milestone zeroGrantRequested', ()=>{

       milestone.claimStatus = 'Pending';
       maxClaims.RCGF = 0;
       maxClaims.DPF = 0;
       milestone.monetary = true;
       milestone.monetarySplit = 60;

       modal = ClaimMilestoneModal.show(milestone, {maxClaims: maxClaims,  grantValue:2000, associatedProject:true, milestoneType:'MonetarySplit'});
       $rootScope.$digest();

       expect(getModalDialog().text()).toContain('CLAIM MILESTONE');
       expect(getModalDialog().text()).toContain('By claiming the milestone you are confirming the milestone has been completed.');
       expect(claimBtn().text()).toEqual('CLAIM MILESTONE')
       expect(dismissBtn().text()).toContain('Close');

       expect(getModalDialog().text()).not.toContain('60% of the project\'s grant claimed with this milestone');
       expect(getModalDialog().text()).not.toContain('Grant Total: £1,200');
       expect(getModalDialog().text()).not.toContain('By claiming the milestone you are confirming the milestone has been completed. Claimed payments will display in the Payments section of GLA OPS once the project changes have been approved.');



     });
     it('Claimed milestone zeroGrantRequested', ()=>{

       milestone.claimStatus = 'Claimed';
       maxClaims.RCGF = 0;
       maxClaims.DPF = 0;
       milestone.monetary = true;
       milestone.monetarySplit = 60;

       modal = ClaimMilestoneModal.show(milestone, {maxClaims: maxClaims,  grantValue:2000, associatedProject:true, milestoneType:'MonetarySplit'});
       $rootScope.$digest();

       expect(getModalDialog().text()).toContain('CLAIMED MILESTONE');
       expect(getModalDialog().text()).toContain('Milestone claims must be cancelled before the milestone can be edited.');
       expect(cancelBtn().text()).toEqual('CANCEL CLAIM')
       expect(dismissBtn().text()).toContain('Close');


     });
   });


   describe('Claim milestone modal: RCGF milestone', ()=>{
     afterEach(()=>{
       getModalDialog().remove();
     });
     it('Claimable RCGF milestone', ()=>{
       milestone.claimStatus = 'Pending';
       maxClaims.RCGF = 1000;

       modal = ClaimMilestoneModal.show(milestone, {maxClaims:maxClaims, milestoneType:'MonetarySplit'});
       $rootScope.$digest();
       expect(getModalDialog().text()).toContain('CLAIM MILESTONE');
       expect(getModalDialog().text()).toContain('Are you applying any funding from other grant sources at this milestone?');
       expect(getModalDialog().text()).toContain('RCGF');
       expect(getModalDialog().text()).toContain('Total unclaimed RCGF: £1,000');
       expect(getModalDialog().text()).toContain('RCGF payment amount £');
       expect(getRCGFInput().length).toEqual(1);
       expect(getModalDialog().text()).not.toContain('Payment amount cannot exceed total unclaimed RCGF');

       expect(claimBtn().text()).toEqual('CLAIM MILESTONE');
       expect(dismissBtn().text()).toContain('Close');
     });
     it('Shows RCGF amount claimed', ()=>{
       milestone.claimStatus = 'Claimed';
       milestone.claimedRcgf = 1000;
       maxClaims.RCGF = 1000;

       modal = ClaimMilestoneModal.show(milestone, {maxClaims:maxClaims, milestoneType:'MonetarySplit'});
       $rootScope.$digest();
       expect(getModalDialog().text()).toContain('CLAIMED MILESTONE');
       expect(getModalDialog().text()).toContain('Funding from grant sources at this milestone');
       expect(getModalDialog().text()).toContain('RCGF: £1,000');
       expect(getModalDialog().text()).toContain('Milestone claims must be cancelled before the milestone can be edited.');
       expect(getRCGFInput().length).toEqual(0);

       expect(getModalDialog().text()).not.toContain('Are you applying any funding from other grant sources at this milestone?');
       expect(getModalDialog().text()).not.toContain('Total unclaimed RCGF: £1,000');
       expect(getModalDialog().text()).not.toContain('RCGF payment amount £');

       expect(cancelBtn().text()).toEqual('CANCEL CLAIM');
       expect(dismissBtn().text()).toContain('Close');
     });

     it('Shows RCGF validation message if amount is too big', ()=>{
       milestone.claimStatus = 'Pending';
       maxClaims.RCGF = 1001;

       modal = ClaimMilestoneModal.show(milestone, {maxClaims:maxClaims, milestoneType:'MonetarySplit'});
       $rootScope.$digest();
       getRCGFInput().val(2000).trigger('input');
       $rootScope.$digest();

       $rootScope.$digest();
       expect(getModalDialog().text()).toContain('CLAIM MILESTONE');
       expect(getModalDialog().text()).toContain('Are you applying any funding from other grant sources at this milestone?');
       expect(getModalDialog().text()).toContain('RCGF');
       expect(getModalDialog().text()).toContain('Total unclaimed RCGF: £1,001');
       expect(getModalDialog().text()).toContain('RCGF payment amount £');
       expect(getModalDialog().text()).toContain('Payment amount cannot exceed total unclaimed RCGF');
       expect(getRCGFInput().length).toEqual(1);

       expect(claimBtn().text()).toEqual('CLAIM MILESTONE');
       expect(dismissBtn().text()).toContain('Close');
     });
   });

   describe('Claim milestone modal: DPF milestone', ()=>{
     afterEach(()=>{
       getModalDialog().remove();
     });
     it('Claimable DPF milestone', ()=>{
       milestone.claimStatus = 'Pending';
       maxClaims.DPF = 1000;

       modal = ClaimMilestoneModal.show(milestone, {maxClaims:maxClaims, milestoneType:'MonetarySplit'});
       $rootScope.$digest();
       expect(getModalDialog().text()).toContain('CLAIM MILESTONE');
       expect(getModalDialog().text()).toContain('Are you applying any funding from other grant sources at this milestone?');
       expect(getModalDialog().text()).toContain('DPF');
       expect(getModalDialog().text()).toContain('Total unclaimed DPF: £1,000');
       expect(getModalDialog().text()).toContain('DPF payment amount £');
       expect(getDPFInput().length).toEqual(1);
       expect(getModalDialog().text()).not.toContain('Payment amount cannot exceed total unclaimed DPF');

       expect(claimBtn().text()).toEqual('CLAIM MILESTONE')
       expect(dismissBtn().text()).toContain('Close');
     });
     it('Shows DPF amount claimed', ()=>{
       milestone.claimStatus = 'Claimed';
       milestone.claimedDpf = 1000;
       maxClaims.DPF = 1000;

       modal = ClaimMilestoneModal.show(milestone, {maxClaims:maxClaims, milestoneType:'MonetarySplit'});
       $rootScope.$digest();
       expect(getModalDialog().text()).toContain('CLAIMED MILESTONE');
       expect(getModalDialog().text()).toContain('Funding from grant sources at this milestone');
       expect(getModalDialog().text()).toContain('DPF: £1,000');
       expect(getModalDialog().text()).toContain('Milestone claims must be cancelled before the milestone can be edited.');
       expect(getDPFInput().length).toEqual(0);

       expect(getModalDialog().text()).not.toContain('Are you applying any funding from other grant sources at this milestone?');
       expect(getModalDialog().text()).not.toContain('Total unclaimed DPF: £1,000');
       expect(getModalDialog().text()).not.toContain('DPF payment amount £');

       expect(cancelBtn().text()).toEqual('CANCEL CLAIM');
       expect(dismissBtn().text()).toContain('Close');
     });

     it('Shows DPF validation message if amount is too big', ()=>{
       milestone.claimStatus = 'Pending';
       maxClaims.DPF = 1000;

       modal = ClaimMilestoneModal.show(milestone, {maxClaims:maxClaims, milestoneType:'MonetarySplit'});

       $rootScope.$digest();
       getDPFInput().val(1500).trigger('input');
       $rootScope.$digest();

       expect(getModalDialog().text()).toContain('CLAIM MILESTONE');
       expect(getModalDialog().text()).toContain('Are you applying any funding from other grant sources at this milestone?');
       expect(getModalDialog().text()).toContain('DPF');
       expect(getModalDialog().text()).toContain('Total unclaimed DPF: £1,000');
       expect(getModalDialog().text()).toContain('DPF payment amount £');
       expect(getModalDialog().text()).toContain('Payment amount cannot exceed total unclaimed DPF');
       expect(getDPFInput().length).toEqual(1);

       expect(claimBtn().text()).toEqual('CLAIM MILESTONE')
       expect(dismissBtn().text()).toContain('Close');

       getModalDialog().remove();
     });
   });

   describe('Non % monetary milestone', ()=>{
     afterEach(()=>{
       getModalDialog().remove();
     });
     it('Shows non % monetary claimable modal (GLA-11774)', ()=> {
       milestone.claimStatus = 'Pending';
       milestone.monetaryValue = 500;
       milestone.monetary = true;
       maxClaims.Grant = 1000;

       modal = ClaimMilestoneModal.show(milestone, {maxClaims:maxClaims, milestoneType:'MonetaryValue'});
       $rootScope.$digest();

       expect(getModalDialog().text()).toContain('CLAIM MILESTONE');
       expect(getModalDialog().text()).toContain('Grant');
       expect(getModalDialog().text()).toContain('Total unclaimed grant available: £1,000');
       expect(getModalDialog().text()).toContain('Grant payment amount £');
       expect(getGrantInput().length).toEqual(1);
       expect(getModalDialog().text()).not.toContain('Payment amount cannot exceed total unclaimed Grant');
       expect(getModalDialog().text()).not.toContain('% of the project\'s grant claimed with this milestone');
       expect(getModalDialog().text()).toContain('By claiming the milestone you are confirming the milestone has been completed. Claimed payments will display in the Payments section of GLA OPS once the project changes have been approved.');
       expect(claimBtn().text()).toEqual('CLAIM MILESTONE');
       expect(dismissBtn().text()).toContain('Close');
     });

     it('Shows modal for non monetary milestone inside MonetarySplit template (GLA-11772)', ()=> {
       milestone.claimStatus = 'Pending';
       milestone.monetarySplit = 0;
       milestone.monetary = false;
       maxClaims.Grant = 1000;

       modal = ClaimMilestoneModal.show(milestone, {maxClaims:maxClaims, milestoneType:'MonetarySplit'});
       $rootScope.$digest();

       expect(getModalDialog().text()).toContain('CLAIM MILESTONE');
       expect(getModalDialog().text()).not.toContain('Grant');
       expect(getModalDialog().text()).not.toContain('Grant payment amount £');
       expect(getModalDialog().text()).not.toContain('% of the project\'s grant claimed with this milestone');
       expect(getGrantInput().length).toEqual(0);
       expect(getModalDialog().text()).toContain('By claiming the milestone you are confirming the milestone has been completed.');
       expect(claimBtn().text()).toEqual('CLAIM MILESTONE');
       expect(dismissBtn().text()).toContain('Close');
     });


     it('Shows non % monetary claimed modal', ()=> {
       milestone.claimStatus = 'Claimed';
       milestone.monetaryValue = 500;
       maxClaims.Grant = 1000;

       modal = ClaimMilestoneModal.show(milestone, {maxClaims:maxClaims, milestoneType:'MonetaryValue'});
       $rootScope.$digest();

       expect(getModalDialog().text()).toContain('CLAIMED MILESTONE');
       expect(getModalDialog().text()).not.toContain('Grant: £1,000');
       expect(getModalDialog().text()).not.toContain('Total unclaimed grant Available: £1,000');
       expect(getModalDialog().text()).not.toContain('Grant payment amount £');
       expect(getGrantInput().length).toEqual(0);
       expect(getModalDialog().text()).not.toContain('By claiming the milestone you are confirming the milestone has been completed. Claimed payments will display in the Payments section of GLA OPS once the project changes have been approved.');
       expect(cancelBtn().text()).toEqual('CANCEL CLAIM')
       expect(claimBtn().text().length).toEqual(0);
       expect(dismissBtn().text()).toContain('Close');
     });

     it('Shows non % monetary max exceeded', ()=> {
       milestone.claimStatus = 'Claimed';

       maxClaims.Grant = 1000;

       modal = ClaimMilestoneModal.show(milestone, {maxClaims:maxClaims, milestoneType:'MonetaryValue'});
       $rootScope.$digest();

       expect(getModalDialog().text()).toContain('CLAIMED MILESTONE');
       expect(getModalDialog().text()).not.toContain('Grant: £1,000');
       expect(getModalDialog().text()).not.toContain('Total unclaimed grant Available: £1,000');
       expect(getModalDialog().text()).not.toContain('Grant payment amount £');
       expect(getGrantInput().length).toEqual(0);
       expect(getModalDialog().text()).not.toContain('By claiming the milestone you are confirming the milestone has been completed. Claimed payments will display in the Payments section of GLA OPS once the project changes have been approved.');
       expect(cancelBtn().text()).toEqual('CANCEL CLAIM')
       expect(claimBtn().text().length).toEqual(0);
       expect(dismissBtn().text()).toContain('Close');
     });
     it('Shows non % monetary amount entered too big', ()=> {
       milestone.claimStatus = 'Pending';
       milestone.monetaryValue = 500;
       milestone.monetary = true;
       maxClaims.Grant = 1000;
       modal = ClaimMilestoneModal.show(milestone, {maxClaims:maxClaims, milestoneType:'MonetaryValue'});

       $rootScope.$digest();
       getGrantInput().val(1500).trigger('input');
       $rootScope.$digest();

       expect(getModalDialog().text()).toContain('CLAIM MILESTONE');
       expect(getModalDialog().text()).toContain('Grant');
       expect(getModalDialog().text()).toContain('Total unclaimed grant available: £1,000');
       expect(getModalDialog().text()).toContain('Grant payment amount £');
       expect(getGrantInput().length).toEqual(1);
       expect(getModalDialog().text()).toContain('By claiming the milestone you are confirming the milestone has been completed. Claimed payments will display in the Payments section of GLA OPS once the project changes have been approved.');
       expect(getModalDialog().text()).toContain('Payment amount cannot exceed total unclaimed Grant');
       expect(claimBtn().text()).toEqual('CLAIM MILESTONE')
       expect(dismissBtn().text()).toContain('Close');

       getModalDialog().remove();
     });
   });


   function getModalDialog() {
     return $('.modal-dialog');
   }
   function claimBtn() {
     return $('.claim-btn');
   }
   function cancelBtn() {
     return $('.cancel-btn');
   }


  //  modal-dialog
  //  modal-header
  //  modal-body
   function message() {
     return $('.confirm-message')
   }

   function approveBtn() {
     return $('.approve-btn');
   }

   function dismissBtn() {
     return $('.dismiss-btn');
   }
   function getRCGFInput() {
     return $('#rcgf-input');
   }
   function getDPFInput() {
     return $('#dpf-input');
   }
   function getGrantInput() {
     return $('#grant-input');
   }
 });
