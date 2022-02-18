/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.refdata

import uk.gov.london.ops.framework.enums.GrantType

// legacy payment sources
const val GRANT = "Grant"
const val RCGF = "RCGF"
const val DPF = "DPF"
const val MOPAC = "MOPAC"
const val ESF = "ESF"
const val BSF = "Business Support Fund"

data class PaymentSource(var name: String? = null,
                         var description: String? = null,
                         var grantType: GrantType? = null,
                         var sendToSap: Boolean = false) {

    fun shouldPaymentSourceBeSentToSAP() = sendToSap

}
