/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */


class PendingPaymentsTestUtil {
  static testData(index, override) {
    let generated = {
      id: Math.random(),
      projectId: index,
      blockId: index,
      projectName: `p${index}`,
      organisationName: `org${index}`,
      programmeName: `programme${index}`,
      category: `category${index}`,
      subCategory: `subCategory${index}`,
      source: `Grant`,
      createdOn: `2017-05-16T11:42:46.073+01:00`
    };
    return _.merge(generated, override);
  }
  static testGroupData(index, overrideGroup, overridePayements) {
    let generated = {
      id: Math.random(),
      declineComments: 'A comment',
      declineReason: {
        category: 'PaymentDeclineReason',
        displayOrder: 1,
        displayValue: 'Incorrect payment amount',
        id: 12
      },
      payments: []
    };
    let toBeAdded = overridePayements || [];
    for (var i = 0; i < toBeAdded.length; i++) {
      generated.payments.push(this.testData(index+'-'+i, toBeAdded[i]));
    }
    return _.merge(generated, overrideGroup);
  }
}

export default PendingPaymentsTestUtil
