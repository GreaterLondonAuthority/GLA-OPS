/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

MilestonesService.$inject = ['$http', 'config', 'moment', '$timeout', '$rootScope'];

function MilestonesService($http, config, moment, $timeout, $rootScope) {

  return {
    claimActions: {
      claim: 1,
      return: 2,
      cancel: 3,
      cancelReclaim: 4
    },

    getMilestoneBlock(projectId, blockId) {
      return $http.get(`${config.basePath}/projects/${projectId}/milestones/${blockId}`);
    },

    claimMilestone(projectId, milestoneId, data) {
      return $http({
        url: `${config.basePath}/projects/${projectId}/milestones/${milestoneId}/claim`,
        method: 'PUT',
        data: data
      });
    },

    cancelClaim(projectId, milestoneId) {
      return $http.put(`${config.basePath}/projects/${projectId}/milestones/${milestoneId}/cancelClaim`);
    },

    cancelReclaim(projectId, milestoneId) {
      return $http.put(`${config.basePath}/projects/${projectId}/milestones/${milestoneId}/cancelReclaim`);
    },

    hasForecastInThePast(milestones) {
      return (milestones || []).some(m => {
        return new Date(m.milestoneDate) <= new Date() && m.milestoneStatus === 'FORECAST';
      });
    },

    attachEvidence(projectId, blockId, milestoneId, fileId) {
      return $http.put(`${config.basePath}/projects/${projectId}/milestones/${blockId}/milestone/${milestoneId}/file/${fileId}`);
    },

    deleteEvidence(projectId, blockId, milestoneId, attachmentId) {
      return $http.delete(`${config.basePath}/projects/${projectId}/milestones/${blockId}/milestone/${milestoneId}/attachment/${attachmentId}`);
    },

    setMilestoneAsNotApplicable(milestone) {
      milestone.notApplicable = true;
      milestone.milestoneDate = null;
      milestone.monetarySplit = 0;
      milestone.claimStatus = null;
      milestone.milestoneStatus = null;
      return milestone;
    },

    /**
     *
     * @param projectBlock
     * @param payments TODO this.payments.content
     */
    convertApiMilestonesToUiModel(milestones, projectBlock, payments, readOnly, isMonetarySplitType, isSplit100, isMonetaryValueType, isMonetaryValueReclaimsEnabled) {
      let tomorrow = moment().add(1, 'day');
      milestones.forEach(m => {
        if (m.milestoneDate) {
          //For milestone status
          m.isDateInPast = moment(m.milestoneDate).isBefore(tomorrow, 'day');
        }

        m.monetarySplit = m.monetarySplit || 0;

        // TODO figure out what to do about pagination
        m.payments = _.filter(payments || [], p => {
          if (m.externalId && p.externalId) {
            return p.externalId === m.externalId;
          } else {
            let originalName = m.monetaryValue ? `Bespoke ${m.summary}` : m.summary;
            let milestoneName = p.reclaim ? `Reclaimed ${originalName}` : originalName;
            return p.subCategory.toLowerCase() === milestoneName.toLowerCase();
          }
        });


        let availableToReclaimByType = projectBlock.availableToReclaimByType;

        m.displayText = m.claimStatus;
        m.hasAction = false;
        m.isCancelable = false;
        m.isClaimable = false;
        m.isRepayable = false;
        m.isReclaimed = false;


        if (m.claimStatus === 'Approved') {
          let isReclaimDisabledForMoneatryValue = !isMonetaryValueReclaimsEnabled && isMonetaryValueType;
          if (!isReclaimDisabledForMoneatryValue && !readOnly && (
              (m.claimedRcgf ? (availableToReclaimByType.RCGF + (m.reclaimedRcgf || 0)) : 0) +
              (m.claimedDpf ? (availableToReclaimByType.DPF + (m.reclaimedDpf || 0)) : 0)
            ) > 0) {
            m.hasRemainingReclaim = true;
            m.hasAction = true;
            m.isRepayable = true;
          } else {
            m.hasAction = false;
          }

          if (isMonetaryValueReclaimsEnabled && isMonetaryValueType && !readOnly && (m.claimedGrant ? (availableToReclaimByType.Grant + (m.reclaimedGrant || 0)) : 0) > 0) {
            m.hasAction = true;
            m.hasRemainingReclaim = true;
            m.isRepayable = true;
          }

          if(m.reclaimedDpf || m.reclaimedRcgf || m.reclaimedGrant){
            m.displayText = 'Reclaimed';
            m.hasAction = true;
            m.isReclaimed = true;
            m.isRepayable = false;
            m.isCancelable = true;
          }
        }

        // Due to some UX complications in another story, we don't want to show
        // the approved modal.
        if (m.claimStatus === 'Claimed') {
          m.hasAction = true;
          m.isCancelable = true;
        }

        if (!readOnly && m.milestoneStatus === 'ACTUAL' && m.claimStatus === 'Pending' ) {
          // m.displayText = 'Claim';
          if (isMonetarySplitType) {
            m.hasAction = isSplit100;
          } else {
            m.hasAction = true;
          }

          if (m.hasAction) {
            m.isClaimable = true;
          }
        }

        if (!m.hasAction && m.notApplicable) {
          m.displayText = 'N/A';
        }

        if (!projectBlock.paymentsEnabled && m.monetary) {
          m.isClaimable = false;
          m.isRepayable = false;
        }

      });
      return milestones;
    },


    /**
     * We show 'claim status' column if at least 1 milestone has claim status (not null) and it is not auto approval and it is isMonetaryValueType or isMonetarySplitType
     * claimStatus is not null after approval.
     *
     * @returns {{Default: {}, Pending: {readOnlyText: string, actionText: string}, Claimed: {}, Approved: {}}}
     */
    getConfig() {
      let config2 = {
        'Default': {
          description: 'If all milestones are null we are not showing. For projects which never were approved',
        },
        'Pending': {
          readOnlyText: 'Pending',
          actionText: 'Claim',
        },
        'Claimed': {},
        'Approved': {}
      };

      return config2;
    }
  }
}

angular.module('GLA')
  .service('MilestonesService', MilestonesService);
